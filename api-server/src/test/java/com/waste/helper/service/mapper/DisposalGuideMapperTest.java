package com.waste.helper.service.mapper;

import static com.waste.helper.domain.DisposalGuideAsserts.*;
import static com.waste.helper.domain.DisposalGuideTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DisposalGuideMapperTest {

    private DisposalGuideMapper disposalGuideMapper;

    @BeforeEach
    void setUp() {
        disposalGuideMapper = new DisposalGuideMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDisposalGuideSample1();
        var actual = disposalGuideMapper.toEntity(disposalGuideMapper.toDto(expected));
        assertDisposalGuideAllPropertiesEquals(expected, actual);
    }
}
