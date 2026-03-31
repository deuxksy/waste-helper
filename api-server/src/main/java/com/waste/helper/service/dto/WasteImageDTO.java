package com.waste.helper.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.WasteImage} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WasteImageDTO implements Serializable {

    private Long id;

    private String originalUrl;

    private String thumbnailUrl;

    private WasteClassificationDTO wasteClassification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
        if (!(o instanceof WasteImageDTO)) {
            return false;
        }

        WasteImageDTO wasteImageDTO = (WasteImageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, wasteImageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WasteImageDTO{" +
            "id=" + getId() +
            ", originalUrl='" + getOriginalUrl() + "'" +
            ", thumbnailUrl='" + getThumbnailUrl() + "'" +
            ", wasteClassification=" + getWasteClassification() +
            "}";
    }
}
