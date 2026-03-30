package com.waste.helper.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.NotificationSetting} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificationSettingDTO implements Serializable {

    private Long id;

    private Boolean enabled;

    private String fcmToken;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationSettingDTO)) {
            return false;
        }

        NotificationSettingDTO notificationSettingDTO = (NotificationSettingDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, notificationSettingDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationSettingDTO{" +
            "id=" + getId() +
            ", enabled='" + getEnabled() + "'" +
            ", fcmToken='" + getFcmToken() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
