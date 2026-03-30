package com.waste.helper.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RegionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Region getRegionSample1() {
        return new Region().id(1L).sido("sido1").sigungu("sigungu1").emdName("emdName1").emdCode("emdCode1");
    }

    public static Region getRegionSample2() {
        return new Region().id(2L).sido("sido2").sigungu("sigungu2").emdName("emdName2").emdCode("emdCode2");
    }

    public static Region getRegionRandomSampleGenerator() {
        return new Region()
            .id(longCount.incrementAndGet())
            .sido(UUID.randomUUID().toString())
            .sigungu(UUID.randomUUID().toString())
            .emdName(UUID.randomUUID().toString())
            .emdCode(UUID.randomUUID().toString());
    }
}
