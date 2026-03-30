package com.waste.helper.domain;

import static com.waste.helper.domain.FeedbackTestSamples.*;
import static com.waste.helper.domain.WasteClassificationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FeedbackTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Feedback.class);
        Feedback feedback1 = getFeedbackSample1();
        Feedback feedback2 = new Feedback();
        assertThat(feedback1).isNotEqualTo(feedback2);

        feedback2.setId(feedback1.getId());
        assertThat(feedback1).isEqualTo(feedback2);

        feedback2 = getFeedbackSample2();
        assertThat(feedback1).isNotEqualTo(feedback2);
    }

    @Test
    void wasteClassificationTest() {
        Feedback feedback = getFeedbackRandomSampleGenerator();
        WasteClassification wasteClassificationBack = getWasteClassificationRandomSampleGenerator();

        feedback.setWasteClassification(wasteClassificationBack);
        assertThat(feedback.getWasteClassification()).isEqualTo(wasteClassificationBack);

        feedback.wasteClassification(null);
        assertThat(feedback.getWasteClassification()).isNull();
    }
}
