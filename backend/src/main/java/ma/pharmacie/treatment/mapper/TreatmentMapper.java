package ma.pharmacie.treatment.mapper;

import ma.pharmacie.lookup.mapper.LookupMapper;
import ma.pharmacie.medication.dto.MedicationSummary;
import ma.pharmacie.medication.entity.Medication;
import ma.pharmacie.treatment.dto.TreatmentResponse;
import ma.pharmacie.treatment.entity.Treatment;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper that turns a {@link Treatment} entity into its API
 * representation. Inner medications are mapped to the lightweight
 * {@link MedicationSummary} projection so payloads stay small.
 *
 * <p>The {@code @IterableMapping(qualifiedByName = "toSummary")} hints below
 * tell MapStruct to reuse the explicit element mapping when generating the
 * list/collection mappings — otherwise it tries to re-derive the mapping and
 * fails on the {@code formLabel} alias.
 */
@Mapper(uses = LookupMapper.class)
public interface TreatmentMapper {

    @Mapping(target = "medications", source = "medications", qualifiedByName = "toSummary")
    TreatmentResponse toResponse(Treatment entity);

    List<TreatmentResponse> toResponse(List<Treatment> entities);

    @Named("toSummary")
    @Mapping(target = "formLabel", source = "form.labelFr")
    MedicationSummary toSummary(Medication medication);

    @IterableMapping(qualifiedByName = "toSummary")
    List<MedicationSummary> toSummary(List<Medication> medications);
}



