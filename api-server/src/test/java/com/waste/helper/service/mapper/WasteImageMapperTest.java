package com.waste.helper.service.mapper;

import static com.waste.helper.domain.WasteImageAsserts.*;
import static com.waste.helper.domain.WasteImageTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WasteImageMapperTest {

    private WasteImageMapper wasteImageMapper;

    @BeforeEach
    void setUp() {
        wasteImageMapper = new WasteImageMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getWasteImageSample1();
        var actual = wasteImageMapper.toEntity(wasteImageMapper.toDto(expected));
        assertWasteImageAllPropertiesEquals(expected, actual);
    }
}
