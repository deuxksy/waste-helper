package com.waste.helper.domain;

import static com.waste.helper.domain.DisposalGuideTestSamples.*;
import static com.waste.helper.domain.RegionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DisposalGuideTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DisposalGuide.class);
        DisposalGuide disposalGuide1 = getDisposalGuideSample1();
        DisposalGuide disposalGuide2 = new DisposalGuide();
        assertThat(disposalGuide1).isNotEqualTo(disposalGuide2);

        disposalGuide2.setId(disposalGuide1.getId());
        assertThat(disposalGuide1).isEqualTo(disposalGuide2);

        disposalGuide2 = getDisposalGuideSample2();
        assertThat(disposalGuide1).isNotEqualTo(disposalGuide2);
    }

    @Test
    void regionTest() {
        DisposalGuide disposalGuide = getDisposalGuideRandomSampleGenerator();
        Region regionBack = getRegionRandomSampleGenerator();

        disposalGuide.setRegion(regionBack);
        assertThat(disposalGuide.getRegion()).isEqualTo(regionBack);

        disposalGuide.region(null);
        assertThat(disposalGuide.getRegion()).isNull();
    }
}
