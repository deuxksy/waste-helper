package com.waste.helper.service.mapper;

import com.waste.helper.domain.WasteClassification;
import com.waste.helper.domain.WasteImage;
import com.waste.helper.service.dto.WasteClassificationDTO;
import com.waste.helper.service.dto.WasteImageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link WasteImage} and its DTO {@link WasteImageDTO}.
 */
@Mapper(componentModel = "spring")
public interface WasteImageMapper extends EntityMapper<WasteImageDTO, WasteImage> {
    @Mapping(target = "wasteClassification", source = "wasteClassification", qualifiedByName = "wasteClassificationId")
    WasteImageDTO toDto(WasteImage s);

    @Named("wasteClassificationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    WasteClassificationDTO toDtoWasteClassificationId(WasteClassification wasteClassification);
}
