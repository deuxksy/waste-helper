package com.waste.helper.service.mapper;

import com.waste.helper.domain.User;
import com.waste.helper.domain.WasteClassification;
import com.waste.helper.service.dto.UserDTO;
import com.waste.helper.service.dto.WasteClassificationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link WasteClassification} and its DTO {@link WasteClassificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface WasteClassificationMapper extends EntityMapper<WasteClassificationDTO, WasteClassification> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    WasteClassificationDTO toDto(WasteClassification s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
