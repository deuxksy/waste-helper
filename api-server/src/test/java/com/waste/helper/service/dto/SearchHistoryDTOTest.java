package com.waste.helper.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SearchHistoryDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SearchHistoryDTO.class);
        SearchHistoryDTO searchHistoryDTO1 = new SearchHistoryDTO();
        searchHistoryDTO1.setId(1L);
        SearchHistoryDTO searchHistoryDTO2 = new SearchHistoryDTO();
        assertThat(searchHistoryDTO1).isNotEqualTo(searchHistoryDTO2);
        searchHistoryDTO2.setId(searchHistoryDTO1.getId());
        assertThat(searchHistoryDTO1).isEqualTo(searchHistoryDTO2);
        searchHistoryDTO2.setId(2L);
        assertThat(searchHistoryDTO1).isNotEqualTo(searchHistoryDTO2);
        searchHistoryDTO1.setId(null);
        assertThat(searchHistoryDTO1).isNotEqualTo(searchHistoryDTO2);
    }
}
