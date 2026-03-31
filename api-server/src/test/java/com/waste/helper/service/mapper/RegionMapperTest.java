package com.waste.helper.service.mapper;

import static com.waste.helper.domain.RegionAsserts.*;
import static com.waste.helper.domain.RegionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegionMapperTest {

    private RegionMapper regionMapper;

    @BeforeEach
    void setUp() {
        regionMapper = new RegionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRegionSample1();
        var actual = regionMapper.toEntity(regionMapper.toDto(expected));
        assertRegionAllPropertiesEquals(expected, actual);
    }
}
