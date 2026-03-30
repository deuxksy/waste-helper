package com.waste.helper.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DisposalGuideDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DisposalGuideDTO.class);
        DisposalGuideDTO disposalGuideDTO1 = new DisposalGuideDTO();
        disposalGuideDTO1.setId(1L);
        DisposalGuideDTO disposalGuideDTO2 = new DisposalGuideDTO();
        assertThat(disposalGuideDTO1).isNotEqualTo(disposalGuideDTO2);
        disposalGuideDTO2.setId(disposalGuideDTO1.getId());
        assertThat(disposalGuideDTO1).isEqualTo(disposalGuideDTO2);
        disposalGuideDTO2.setId(2L);
        assertThat(disposalGuideDTO1).isNotEqualTo(disposalGuideDTO2);
        disposalGuideDTO1.setId(null);
        assertThat(disposalGuideDTO1).isNotEqualTo(disposalGuideDTO2);
    }
}
