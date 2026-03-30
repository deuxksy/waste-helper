package com.waste.helper.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.FavoriteRegion} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FavoriteRegionDTO implements Serializable {

    private Long id;

    private UserDTO user;

    private RegionDTO region;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public RegionDTO getRegion() {
        return region;
    }

    public void setRegion(RegionDTO region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FavoriteRegionDTO)) {
            return false;
        }

        FavoriteRegionDTO favoriteRegionDTO = (FavoriteRegionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, favoriteRegionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FavoriteRegionDTO{" +
            "id=" + getId() +
            ", user=" + getUser() +
            ", region=" + getRegion() +
            "}";
    }
}
