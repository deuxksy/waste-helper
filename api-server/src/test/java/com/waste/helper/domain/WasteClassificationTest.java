package com.waste.helper.domain;

import static com.waste.helper.domain.FeedbackTestSamples.*;
import static com.waste.helper.domain.WasteClassificationTestSamples.*;
import static com.waste.helper.domain.WasteImageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.waste.helper.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WasteClassificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WasteClassification.class);
        WasteClassification wasteClassification1 = getWasteClassificationSample1();
        WasteClassification wasteClassification2 = new WasteClassification();
        assertThat(wasteClassification1).isNotEqualTo(wasteClassification2);

        wasteClassification2.setId(wasteClassification1.getId());
        assertThat(wasteClassification1).isEqualTo(wasteClassification2);

        wasteClassification2 = getWasteClassificationSample2();
        assertThat(wasteClassification1).isNotEqualTo(wasteClassification2);
    }

    @Test
    void wasteImagesTest() {
        WasteClassification wasteClassification = getWasteClassificationRandomSampleGenerator();
        WasteImage wasteImageBack = getWasteImageRandomSampleGenerator();

        wasteClassification.addWasteImages(wasteImageBack);
        assertThat(wasteClassification.getWasteImageses()).containsOnly(wasteImageBack);
        assertThat(wasteImageBack.getWasteClassification()).isEqualTo(wasteClassification);

        wasteClassification.removeWasteImages(wasteImageBack);
        assertThat(wasteClassification.getWasteImageses()).doesNotContain(wasteImageBack);
        assertThat(wasteImageBack.getWasteClassification()).isNull();

        wasteClassification.wasteImageses(new HashSet<>(Set.of(wasteImageBack)));
        assertThat(wasteClassification.getWasteImageses()).containsOnly(wasteImageBack);
        assertThat(wasteImageBack.getWasteClassification()).isEqualTo(wasteClassification);

        wasteClassification.setWasteImageses(new HashSet<>());
        assertThat(wasteClassification.getWasteImageses()).doesNotContain(wasteImageBack);
        assertThat(wasteImageBack.getWasteClassification()).isNull();
    }

    @Test
    void feedbacksTest() {
        WasteClassification wasteClassification = getWasteClassificationRandomSampleGenerator();
        Feedback feedbackBack = getFeedbackRandomSampleGenerator();

        wasteClassification.addFeedbacks(feedbackBack);
        assertThat(wasteClassification.getFeedbackses()).containsOnly(feedbackBack);
        assertThat(feedbackBack.getWasteClassification()).isEqualTo(wasteClassification);

        wasteClassification.removeFeedbacks(feedbackBack);
        assertThat(wasteClassification.getFeedbackses()).doesNotContain(feedbackBack);
        assertThat(feedbackBack.getWasteClassification()).isNull();

        wasteClassification.feedbackses(new HashSet<>(Set.of(feedbackBack)));
        assertThat(wasteClassification.getFeedbackses()).containsOnly(feedbackBack);
        assertThat(feedbackBack.getWasteClassification()).isEqualTo(wasteClassification);

        wasteClassification.setFeedbackses(new HashSet<>());
        assertThat(wasteClassification.getFeedbackses()).doesNotContain(feedbackBack);
        assertThat(feedbackBack.getWasteClassification()).isNull();
    }
}
