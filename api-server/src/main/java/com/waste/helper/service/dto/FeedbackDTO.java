package com.waste.helper.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.Feedback} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FeedbackDTO implements Serializable {

    private Long id;

    private Boolean isAccurate;

    private String correctedClass;

    private String comment;

    private UserDTO user;

    private WasteClassificationDTO wasteClassification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsAccurate() {
        return isAccurate;
    }

    public void setIsAccurate(Boolean isAccurate) {
        this.isAccurate = isAccurate;
    }

    public String getCorrectedClass() {
        return correctedClass;
    }

    public void setCorrectedClass(String correctedClass) {
        this.correctedClass = correctedClass;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public WasteClassificationDTO getWasteClassification() {
        return wasteClassification;
    }

    public void setWasteClassification(WasteClassificationDTO wasteClassification) {
        this.wasteClassification = wasteClassification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeedbackDTO)) {
            return false;
        }

        FeedbackDTO feedbackDTO = (FeedbackDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, feedbackDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FeedbackDTO{" +
            "id=" + getId() +
            ", isAccurate='" + getIsAccurate() + "'" +
            ", correctedClass='" + getCorrectedClass() + "'" +
            ", comment='" + getComment() + "'" +
            ", user=" + getUser() +
            ", wasteClassification=" + getWasteClassification() +
            "}";
    }
}
