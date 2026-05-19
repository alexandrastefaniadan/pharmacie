package ma.pharmacie.medication;

import ma.pharmacie.medication.dto.MedicationCreateRequest;
import ma.pharmacie.medication.dto.MedicationFilter;
import ma.pharmacie.medication.dto.MedicationResponse;
import ma.pharmacie.medication.service.MedicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the medication CRUD flow and the AND-combined filter
 * (e.g. "syrup → cough → child") against a real PostgreSQL via Testcontainers.
 */
@SpringBootTest
@Testcontainers
@Transactional
class MedicationServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("pharmacie")
                    .withUsername("pharmacie")
                    .withPassword("pharmacie");

    @DynamicPropertySource
    static void dataSourceProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired MedicationService service;
    @Autowired JdbcTemplate jdbc;

    @Test
    void create_then_filter_by_form_indication_and_ageGroup() {
        Integer syrupId   = lookupId("pharmaceutical_form", "SYRUP");
        Integer creamId   = lookupId("pharmaceutical_form", "CREAM");
        Integer coughDry  = lookupId("indication",          "COUGH_DRY");
        Integer wound     = lookupId("indication",          "WOUND_HEALING");
        Integer childId   = lookupId("age_group",           "CHILD");
        Integer adultId   = lookupId("age_group",           "ADULT");

        MedicationResponse syrup = service.create(new MedicationCreateRequest(
                "Toplexil enfant", "oxomemazine", "1.65 mg/5ml", null,
                false, null, syrupId, Set.of(childId), null, Set.of(coughDry), null, null));

        service.create(new MedicationCreateRequest(
                "Cicatryl",       "trolamine",   null, null,
                false, null, creamId, Set.of(adultId), null, Set.of(wound), null, null));

        // 1) filter that should match exactly one (syrup + dry cough + child)
        Page<MedicationResponse> hits = service.search(
                new MedicationFilter(null, Set.of(syrupId), Set.of(childId), null,
                                     Set.of(coughDry), null, null),
                PageRequest.of(0, 10, Sort.by("name")));

        assertThat(hits.getTotalElements()).isEqualTo(1);
        assertThat(hits.getContent().get(0).id()).isEqualTo(syrup.id());

        // 2) free-text search on name (case-insensitive, partial)
        Page<MedicationResponse> textHits = service.search(
                new MedicationFilter("topl", null, null, null, null, null, null),
                PageRequest.of(0, 10));
        assertThat(textHits.getTotalElements()).isEqualTo(1);

        // 3) no filter → 2 rows
        assertThat(service.search(MedicationFilter.empty(),
                PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
    }

    @Test
    void soft_delete_hides_row_from_subsequent_queries() {
        Integer syrupId = lookupId("pharmaceutical_form", "SYRUP");
        MedicationResponse m = service.create(new MedicationCreateRequest(
                "Doliprane sirop", "paracétamol", "2.4%", null,
                false, null, syrupId, null, null, null, null, null));

        service.delete(m.id());

        Page<MedicationResponse> visible = service.search(MedicationFilter.empty(),
                PageRequest.of(0, 10));
        assertThat(visible.getContent()).extracting(MedicationResponse::id).doesNotContain(m.id());

        // row is still present, only flagged
        Integer alive   = jdbc.queryForObject("SELECT COUNT(*) FROM medication WHERE id = ? AND deleted_at IS NULL", Integer.class, m.id());
        Integer deleted = jdbc.queryForObject("SELECT COUNT(*) FROM medication WHERE id = ? AND deleted_at IS NOT NULL", Integer.class, m.id());
        assertThat(alive).isZero();
        assertThat(deleted).isEqualTo(1);
    }

    private Integer lookupId(String table, String code) {
        return jdbc.queryForObject(
                "SELECT id FROM " + table + " WHERE code = ?",
                Integer.class, code);
    }
}

