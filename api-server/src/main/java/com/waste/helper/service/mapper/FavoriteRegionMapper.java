package com.waste.helper.service.mapper;

import com.waste.helper.domain.FavoriteRegion;
import com.waste.helper.domain.Region;
import com.waste.helper.domain.User;
import com.waste.helper.service.dto.FavoriteRegionDTO;
import com.waste.helper.service.dto.RegionDTO;
import com.waste.helper.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link FavoriteRegion} and its DTO {@link FavoriteRegionDTO}.
 */
@Mapper(componentModel = "spring")
public interface FavoriteRegionMapper extends EntityMapper<FavoriteRegionDTO, FavoriteRegion> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "region", source = "region", qualifiedByName = "regionEmdName")
    FavoriteRegionDTO toDto(FavoriteRegion s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("regionEmdName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "emdName", source = "emdName")
    RegionDTO toDtoRegionEmdName(Region region);
}
