package com.waste.helper.domain;

import static com.waste.helper.domain.WasteClassificationTestSamples.*;
import static com.waste.helper.domain.WasteImageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WasteImageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WasteImage.class);
        WasteImage wasteImage1 = getWasteImageSample1();
        WasteImage wasteImage2 = new WasteImage();
        assertThat(wasteImage1).isNotEqualTo(wasteImage2);

        wasteImage2.setId(wasteImage1.getId());
        assertThat(wasteImage1).isEqualTo(wasteImage2);

        wasteImage2 = getWasteImageSample2();
        assertThat(wasteImage1).isNotEqualTo(wasteImage2);
    }

    @Test
    void wasteClassificationTest() {
        WasteImage wasteImage = getWasteImageRandomSampleGenerator();
        WasteClassification wasteClassificationBack = getWasteClassificationRandomSampleGenerator();

        wasteImage.setWasteClassification(wasteClassificationBack);
        assertThat(wasteImage.getWasteClassification()).isEqualTo(wasteClassificationBack);

        wasteImage.wasteClassification(null);
        assertThat(wasteImage.getWasteClassification()).isNull();
    }
}
