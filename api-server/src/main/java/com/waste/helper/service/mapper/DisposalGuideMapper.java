package com.waste.helper.service.mapper;

import com.waste.helper.domain.DisposalGuide;
import com.waste.helper.domain.Region;
import com.waste.helper.service.dto.DisposalGuideDTO;
import com.waste.helper.service.dto.RegionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DisposalGuide} and its DTO {@link DisposalGuideDTO}.
 */
@Mapper(componentModel = "spring")
public interface DisposalGuideMapper extends EntityMapper<DisposalGuideDTO, DisposalGuide> {
    @Mapping(target = "region", source = "region", qualifiedByName = "regionId")
    DisposalGuideDTO toDto(DisposalGuide s);

    @Named("regionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    RegionDTO toDtoRegionId(Region region);
}
