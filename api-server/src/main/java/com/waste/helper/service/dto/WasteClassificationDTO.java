package com.waste.helper.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.WasteClassification} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WasteClassificationDTO implements Serializable {

    private Long id;

    @NotNull
    private String detectedClass;

    @NotNull
    @DecimalMin(value = "0")
    private Float confidence;

    private String imageUrl;

    @Lob
    private String detailResult;

    @NotNull
    private Instant classifiedAt;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetectedClass() {
        return detectedClass;
    }

    public void setDetectedClass(String detectedClass) {
        this.detectedClass = detectedClass;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDetailResult() {
        return detailResult;
    }

    public void setDetailResult(String detailResult) {
        this.detailResult = detailResult;
    }

    public Instant getClassifiedAt() {
        return classifiedAt;
    }

    public void setClassifiedAt(Instant classifiedAt) {
        this.classifiedAt = classifiedAt;
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
        if (!(o instanceof WasteClassificationDTO)) {
            return false;
        }

        WasteClassificationDTO wasteClassificationDTO = (WasteClassificationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, wasteClassificationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WasteClassificationDTO{" +
            "id=" + getId() +
            ", detectedClass='" + getDetectedClass() + "'" +
            ", confidence=" + getConfidence() +
            ", imageUrl='" + getImageUrl() + "'" +
            ", detailResult='" + getDetailResult() + "'" +
            ", classifiedAt='" + getClassifiedAt() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
