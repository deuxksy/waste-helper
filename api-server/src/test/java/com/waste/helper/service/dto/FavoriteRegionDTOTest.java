package com.waste.helper.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriteRegionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FavoriteRegionDTO.class);
        FavoriteRegionDTO favoriteRegionDTO1 = new FavoriteRegionDTO();
        favoriteRegionDTO1.setId(1L);
        FavoriteRegionDTO favoriteRegionDTO2 = new FavoriteRegionDTO();
        assertThat(favoriteRegionDTO1).isNotEqualTo(favoriteRegionDTO2);
        favoriteRegionDTO2.setId(favoriteRegionDTO1.getId());
        assertThat(favoriteRegionDTO1).isEqualTo(favoriteRegionDTO2);
        favoriteRegionDTO2.setId(2L);
        assertThat(favoriteRegionDTO1).isNotEqualTo(favoriteRegionDTO2);
        favoriteRegionDTO1.setId(null);
        assertThat(favoriteRegionDTO1).isNotEqualTo(favoriteRegionDTO2);
    }
}
