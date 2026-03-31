package com.waste.helper.service.mapper;

import com.waste.helper.domain.Feedback;
import com.waste.helper.domain.User;
import com.waste.helper.domain.WasteClassification;
import com.waste.helper.service.dto.FeedbackDTO;
import com.waste.helper.service.dto.UserDTO;
import com.waste.helper.service.dto.WasteClassificationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Feedback} and its DTO {@link FeedbackDTO}.
 */
@Mapper(componentModel = "spring")
public interface FeedbackMapper extends EntityMapper<FeedbackDTO, Feedback> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "wasteClassification", source = "wasteClassification", qualifiedByName = "wasteClassificationId")
    FeedbackDTO toDto(Feedback s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("wasteClassificationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    WasteClassificationDTO toDtoWasteClassificationId(WasteClassification wasteClassification);
}
