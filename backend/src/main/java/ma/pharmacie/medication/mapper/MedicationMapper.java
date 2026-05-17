package ma.pharmacie.medication.mapper;

import ma.pharmacie.lookup.mapper.LookupMapper;
import ma.pharmacie.medication.dto.MedicationResponse;
import ma.pharmacie.medication.entity.Medication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = LookupMapper.class)
public interface MedicationMapper {

    @Mapping(target = "parapharmacy", source = "parapharmacy")
    MedicationResponse toResponse(Medication entity);

    List<MedicationResponse> toResponse(List<Medication> entities);
}

