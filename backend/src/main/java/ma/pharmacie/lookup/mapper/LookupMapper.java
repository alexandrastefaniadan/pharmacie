package ma.pharmacie.lookup.mapper;

import ma.pharmacie.lookup.dto.LookupDto;
import ma.pharmacie.lookup.entity.AbstractLookup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface LookupMapper {

    LookupDto toDto(AbstractLookup entity);

    List<LookupDto> toDto(List<? extends AbstractLookup> entities);
}

