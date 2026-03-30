package com.waste.helper.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationSettingTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static NotificationSetting getNotificationSettingSample1() {
        return new NotificationSetting().id(1L).fcmToken("fcmToken1");
    }

    public static NotificationSetting getNotificationSettingSample2() {
        return new NotificationSetting().id(2L).fcmToken("fcmToken2");
    }

    public static NotificationSetting getNotificationSettingRandomSampleGenerator() {
        return new NotificationSetting().id(longCount.incrementAndGet()).fcmToken(UUID.randomUUID().toString());
    }
}
