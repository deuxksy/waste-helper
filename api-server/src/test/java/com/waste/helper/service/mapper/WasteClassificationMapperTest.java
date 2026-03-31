package com.waste.helper.service.mapper;

import static com.waste.helper.domain.WasteClassificationAsserts.*;
import static com.waste.helper.domain.WasteClassificationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WasteClassificationMapperTest {

    private WasteClassificationMapper wasteClassificationMapper;

    @BeforeEach
    void setUp() {
        wasteClassificationMapper = new WasteClassificationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getWasteClassificationSample1();
        var actual = wasteClassificationMapper.toEntity(wasteClassificationMapper.toDto(expected));
        assertWasteClassificationAllPropertiesEquals(expected, actual);
    }
}
