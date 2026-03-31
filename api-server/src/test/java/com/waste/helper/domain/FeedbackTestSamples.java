package com.waste.helper.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FeedbackTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Feedback getFeedbackSample1() {
        return new Feedback().id(1L).correctedClass("correctedClass1").comment("comment1");
    }

    public static Feedback getFeedbackSample2() {
        return new Feedback().id(2L).correctedClass("correctedClass2").comment("comment2");
    }

    public static Feedback getFeedbackRandomSampleGenerator() {
        return new Feedback()
            .id(longCount.incrementAndGet())
            .correctedClass(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString());
    }
}
