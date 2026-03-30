package com.waste.helper.service.mapper;

import com.waste.helper.domain.SearchHistory;
import com.waste.helper.domain.User;
import com.waste.helper.service.dto.SearchHistoryDTO;
import com.waste.helper.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SearchHistory} and its DTO {@link SearchHistoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface SearchHistoryMapper extends EntityMapper<SearchHistoryDTO, SearchHistory> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    SearchHistoryDTO toDto(SearchHistory s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
