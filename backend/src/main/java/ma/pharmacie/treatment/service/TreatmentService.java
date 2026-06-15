package ma.pharmacie.treatment.service;

import lombok.RequiredArgsConstructor;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.common.exception.ConflictException;
import ma.pharmacie.common.exception.NotFoundException;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.lookup.repo.IndicationRepository;
import ma.pharmacie.medication.entity.Medication;
import ma.pharmacie.medication.repo.MedicationRepository;
import ma.pharmacie.treatment.dto.TreatmentCreateRequest;
import ma.pharmacie.treatment.dto.TreatmentFilter;
import ma.pharmacie.treatment.dto.TreatmentResponse;
import ma.pharmacie.treatment.dto.TreatmentUpdateRequest;
import ma.pharmacie.treatment.entity.Treatment;
import ma.pharmacie.treatment.mapper.TreatmentMapper;
import ma.pharmacie.treatment.repo.TreatmentRepository;
import ma.pharmacie.treatment.spec.TreatmentSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for treatments: CRUD, soft delete, paged
 * specification-driven search.
 *
 * <p>Strong invariants enforced here:
 * <ul>
 *   <li>{@code name} is unique (case-insensitive) within a usage type.</li>
 *   <li>Every medication linked to a treatment must share that treatment's
 *       {@link UsageType}. A treatment is therefore either fully human or
 *       fully veterinary — no mixed bundles.</li>
 *   <li>The order of medications sent by the client is preserved in the DB
 *       via the {@code @OrderColumn} on the join table.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TreatmentService {

    private final TreatmentRepository treatmentRepo;
    private final MedicationRepository medicationRepo;
    private final IndicationRepository indicationRepo;
    private final TreatmentMapper mapper;

    // -------------------- queries --------------------

    @Transactional(readOnly = true)
    public Page<TreatmentResponse> search(TreatmentFilter filter, Pageable pageable) {
        return treatmentRepo.findAll(TreatmentSpecifications.from(filter), pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TreatmentResponse getById(UUID id) {
        return mapper.toResponse(loadOrThrow(id));
    }

    // -------------------- commands --------------------

    public TreatmentResponse create(TreatmentCreateRequest req) {
        String name = req.name().trim();
        UsageType usageType = req.usageType();
        if (treatmentRepo.existsByNameIgnoreCaseAndUsageType(name, usageType)) {
            throw new ConflictException(
                    "A %s treatment with name '%s' already exists."
                            .formatted(usageType.name().toLowerCase(), name));
        }

        Treatment t = Treatment.builder()
                .name(name)
                .description(emptyToNull(req.description()))
                .notes(emptyToNull(req.notes()))
                .usageType(usageType)
                .build();

        applyMedications(t, req.medicationIds());
        applyIndications(t, req.indicationIds());

        return mapper.toResponse(treatmentRepo.save(t));
    }

    public TreatmentResponse update(UUID id, TreatmentUpdateRequest req) {
        Treatment t = loadOrThrow(id);

        if (t.getVersion() != req.version()) {
            throw new ConflictException(
                    "Treatment has been modified by someone else. Reload and try again.");
        }

        String newName = req.name().trim();
        UsageType newUsageType = req.usageType();
        boolean nameOrTypeChanged = !newName.equalsIgnoreCase(t.getName())
                || newUsageType != t.getUsageType();
        if (nameOrTypeChanged
                && treatmentRepo.existsByNameIgnoreCaseAndUsageType(newName, newUsageType)) {
            throw new ConflictException(
                    "A %s treatment with name '%s' already exists."
                            .formatted(newUsageType.name().toLowerCase(), newName));
        }

        t.setName(newName);
        t.setDescription(emptyToNull(req.description()));
        t.setNotes(emptyToNull(req.notes()));
        t.setUsageType(newUsageType);

        applyMedications(t, req.medicationIds());
        applyIndications(t, req.indicationIds());

        return mapper.toResponse(t);
    }

    /** Soft delete — Hibernate's {@code @SQLDelete} flips {@code deleted_at}. */
    public void delete(UUID id) {
        Treatment t = loadOrThrow(id);
        treatmentRepo.delete(t);
    }

    // -------------------- helpers --------------------

    private Treatment loadOrThrow(UUID id) {
        return treatmentRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("Treatment", id));
    }

    /**
     * Replace the medication list while preserving the order the client sent
     * and rejecting any medication whose {@link UsageType} doesn't match the
     * treatment's usage type.
     *
     * <p>We re-fetch the entities by id, sort them in the requested order,
     * then reuse the existing managed list (clear + addAll) so JPA detects
     * the change and rewrites the join table — including the {@code position}
     * column managed by {@code @OrderColumn}.
     */
    private void applyMedications(Treatment t, List<UUID> ids) {
        // The DTO marks medicationIds as @NotEmpty, but be defensive.
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("A treatment must contain at least one medication.");
        }

        // De-duplicate while preserving order; useful if the UI accidentally
        // sends the same id twice.
        List<UUID> orderedIds = ids.stream().distinct().toList();

        List<Medication> found = medicationRepo.findAllById(orderedIds);
        if (found.size() != orderedIds.size()) {
            Set<UUID> foundIds = found.stream().map(Medication::getId).collect(Collectors.toSet());
            Set<UUID> missing = new HashSet<>(orderedIds);
            missing.removeAll(foundIds);
            throw NotFoundException.of("Medication", missing);
        }

        // Coherence: every med must share the treatment's usage type.
        List<String> wrong = found.stream()
                .filter(m -> m.getUsageType() != t.getUsageType())
                .map(m -> m.getName() + " (" + m.getUsageType() + ")")
                .toList();
        if (!wrong.isEmpty()) {
            throw new ConflictException(
                    "These medications don't match the treatment's usage type ("
                            + t.getUsageType() + "): " + String.join(", ", wrong));
        }

        // Re-sort according to the client-supplied order.
        Map<UUID, Medication> byId = new LinkedHashMap<>();
        for (Medication m : found) byId.put(m.getId(), m);
        List<Medication> orderedMeds = new ArrayList<>(orderedIds.size());
        for (UUID id : orderedIds) orderedMeds.add(byId.get(id));

        List<Medication> managed = t.getMedications();
        managed.clear();
        managed.addAll(orderedMeds);
    }

    private void applyIndications(Treatment t, Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            t.getIndications().clear();
            return;
        }
        Set<Indication> loaded = new HashSet<>(indicationRepo.findAllById(ids));
        if (loaded.size() != ids.size()) {
            throw NotFoundException.of("Indication", ids);
        }
        t.setIndications(loaded);
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

