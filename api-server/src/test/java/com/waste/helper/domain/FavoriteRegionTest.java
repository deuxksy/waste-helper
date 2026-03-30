package com.waste.helper.domain;

import static com.waste.helper.domain.FavoriteRegionTestSamples.*;
import static com.waste.helper.domain.RegionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriteRegionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FavoriteRegion.class);
        FavoriteRegion favoriteRegion1 = getFavoriteRegionSample1();
        FavoriteRegion favoriteRegion2 = new FavoriteRegion();
        assertThat(favoriteRegion1).isNotEqualTo(favoriteRegion2);

        favoriteRegion2.setId(favoriteRegion1.getId());
        assertThat(favoriteRegion1).isEqualTo(favoriteRegion2);

        favoriteRegion2 = getFavoriteRegionSample2();
        assertThat(favoriteRegion1).isNotEqualTo(favoriteRegion2);
    }

    @Test
    void regionTest() {
        FavoriteRegion favoriteRegion = getFavoriteRegionRandomSampleGenerator();
        Region regionBack = getRegionRandomSampleGenerator();

        favoriteRegion.setRegion(regionBack);
        assertThat(favoriteRegion.getRegion()).isEqualTo(regionBack);

        favoriteRegion.region(null);
        assertThat(favoriteRegion.getRegion()).isNull();
    }
}
