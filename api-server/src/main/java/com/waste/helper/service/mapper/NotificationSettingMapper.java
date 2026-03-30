package com.waste.helper.service.mapper;

import com.waste.helper.domain.NotificationSetting;
import com.waste.helper.domain.User;
import com.waste.helper.service.dto.NotificationSettingDTO;
import com.waste.helper.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link NotificationSetting} and its DTO {@link NotificationSettingDTO}.
 */
@Mapper(componentModel = "spring")
public interface NotificationSettingMapper extends EntityMapper<NotificationSettingDTO, NotificationSetting> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    NotificationSettingDTO toDto(NotificationSetting s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
