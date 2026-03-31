package com.waste.helper.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class WasteClassificationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static WasteClassification getWasteClassificationSample1() {
        return new WasteClassification().id(1L).detectedClass("detectedClass1").imageUrl("imageUrl1");
    }

    public static WasteClassification getWasteClassificationSample2() {
        return new WasteClassification().id(2L).detectedClass("detectedClass2").imageUrl("imageUrl2");
    }

    public static WasteClassification getWasteClassificationRandomSampleGenerator() {
        return new WasteClassification()
            .id(longCount.incrementAndGet())
            .detectedClass(UUID.randomUUID().toString())
            .imageUrl(UUID.randomUUID().toString());
    }
}
