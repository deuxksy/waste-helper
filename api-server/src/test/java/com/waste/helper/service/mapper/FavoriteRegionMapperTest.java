package com.waste.helper.service.mapper;

import static com.waste.helper.domain.FavoriteRegionAsserts.*;
import static com.waste.helper.domain.FavoriteRegionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FavoriteRegionMapperTest {

    private FavoriteRegionMapper favoriteRegionMapper;

    @BeforeEach
    void setUp() {
        favoriteRegionMapper = new FavoriteRegionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFavoriteRegionSample1();
        var actual = favoriteRegionMapper.toEntity(favoriteRegionMapper.toDto(expected));
        assertFavoriteRegionAllPropertiesEquals(expected, actual);
    }
}
