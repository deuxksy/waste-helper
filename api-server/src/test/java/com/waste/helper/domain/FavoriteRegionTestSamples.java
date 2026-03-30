package com.waste.helper.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FavoriteRegionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static FavoriteRegion getFavoriteRegionSample1() {
        return new FavoriteRegion().id(1L);
    }

    public static FavoriteRegion getFavoriteRegionSample2() {
        return new FavoriteRegion().id(2L);
    }

    public static FavoriteRegion getFavoriteRegionRandomSampleGenerator() {
        return new FavoriteRegion().id(longCount.incrementAndGet());
    }
}
