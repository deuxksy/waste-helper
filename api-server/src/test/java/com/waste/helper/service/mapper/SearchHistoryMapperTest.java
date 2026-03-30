package com.waste.helper.service.mapper;

import static com.waste.helper.domain.SearchHistoryAsserts.*;
import static com.waste.helper.domain.SearchHistoryTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchHistoryMapperTest {

    private SearchHistoryMapper searchHistoryMapper;

    @BeforeEach
    void setUp() {
        searchHistoryMapper = new SearchHistoryMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSearchHistorySample1();
        var actual = searchHistoryMapper.toEntity(searchHistoryMapper.toDto(expected));
        assertSearchHistoryAllPropertiesEquals(expected, actual);
    }
}
