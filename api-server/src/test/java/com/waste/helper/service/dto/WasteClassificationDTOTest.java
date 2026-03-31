package com.waste.helper.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WasteClassificationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(WasteClassificationDTO.class);
        WasteClassificationDTO wasteClassificationDTO1 = new WasteClassificationDTO();
        wasteClassificationDTO1.setId(1L);
        WasteClassificationDTO wasteClassificationDTO2 = new WasteClassificationDTO();
        assertThat(wasteClassificationDTO1).isNotEqualTo(wasteClassificationDTO2);
        wasteClassificationDTO2.setId(wasteClassificationDTO1.getId());
        assertThat(wasteClassificationDTO1).isEqualTo(wasteClassificationDTO2);
        wasteClassificationDTO2.setId(2L);
        assertThat(wasteClassificationDTO1).isNotEqualTo(wasteClassificationDTO2);
        wasteClassificationDTO1.setId(null);
        assertThat(wasteClassificationDTO1).isNotEqualTo(wasteClassificationDTO2);
    }
}
