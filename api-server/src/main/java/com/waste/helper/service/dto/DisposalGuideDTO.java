package com.waste.helper.service.dto;

import com.waste.helper.domain.enumeration.Source;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.DisposalGuide} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DisposalGuideDTO implements Serializable {

    private Long id;

    @NotNull
    private String wasteType;

    @Lob
    private String disposalMethod;

    private Source source;

    private RegionDTO region;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public RegionDTO getRegion() {
        return region;
    }

    public void setRegion(RegionDTO region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DisposalGuideDTO)) {
            return false;
        }

        DisposalGuideDTO disposalGuideDTO = (DisposalGuideDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, disposalGuideDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DisposalGuideDTO{" +
            "id=" + getId() +
            ", wasteType='" + getWasteType() + "'" +
            ", disposalMethod='" + getDisposalMethod() + "'" +
            ", source='" + getSource() + "'" +
            ", region=" + getRegion() +
            "}";
    }
}
