package com.waste.helper.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DisposalGuideTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static DisposalGuide getDisposalGuideSample1() {
        return new DisposalGuide().id(1L).wasteType("wasteType1");
    }

    public static DisposalGuide getDisposalGuideSample2() {
        return new DisposalGuide().id(2L).wasteType("wasteType2");
    }

    public static DisposalGuide getDisposalGuideRandomSampleGenerator() {
        return new DisposalGuide().id(longCount.incrementAndGet()).wasteType(UUID.randomUUID().toString());
    }
}
