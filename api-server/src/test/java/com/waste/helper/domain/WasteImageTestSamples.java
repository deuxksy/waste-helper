package com.waste.helper.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class WasteImageTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static WasteImage getWasteImageSample1() {
        return new WasteImage().id(1L).originalUrl("originalUrl1").thumbnailUrl("thumbnailUrl1");
    }

    public static WasteImage getWasteImageSample2() {
        return new WasteImage().id(2L).originalUrl("originalUrl2").thumbnailUrl("thumbnailUrl2");
    }

    public static WasteImage getWasteImageRandomSampleGenerator() {
        return new WasteImage()
            .id(longCount.incrementAndGet())
            .originalUrl(UUID.randomUUID().toString())
            .thumbnailUrl(UUID.randomUUID().toString());
    }
}
