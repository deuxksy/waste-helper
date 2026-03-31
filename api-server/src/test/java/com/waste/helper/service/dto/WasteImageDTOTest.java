package com.waste.helper.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WasteImageDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(WasteImageDTO.class);
        WasteImageDTO wasteImageDTO1 = new WasteImageDTO();
        wasteImageDTO1.setId(1L);
        WasteImageDTO wasteImageDTO2 = new WasteImageDTO();
        assertThat(wasteImageDTO1).isNotEqualTo(wasteImageDTO2);
        wasteImageDTO2.setId(wasteImageDTO1.getId());
        assertThat(wasteImageDTO1).isEqualTo(wasteImageDTO2);
        wasteImageDTO2.setId(2L);
        assertThat(wasteImageDTO1).isNotEqualTo(wasteImageDTO2);
        wasteImageDTO1.setId(null);
        assertThat(wasteImageDTO1).isNotEqualTo(wasteImageDTO2);
    }
}
