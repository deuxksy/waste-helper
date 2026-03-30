package com.waste.helper.service.mapper;

import static com.waste.helper.domain.NotificationSettingAsserts.*;
import static com.waste.helper.domain.NotificationSettingTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationSettingMapperTest {

    private NotificationSettingMapper notificationSettingMapper;

    @BeforeEach
    void setUp() {
        notificationSettingMapper = new NotificationSettingMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getNotificationSettingSample1();
        var actual = notificationSettingMapper.toEntity(notificationSettingMapper.toDto(expected));
        assertNotificationSettingAllPropertiesEquals(expected, actual);
    }
}
