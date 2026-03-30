package com.waste.helper.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.Region} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RegionDTO implements Serializable {

    private Long id;

    private String sido;

    private String sigungu;

    private String emdName;

    private String emdCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSido() {
        return sido;
    }

    public void setSido(String sido) {
        this.sido = sido;
    }

    public String getSigungu() {
        return sigungu;
    }

    public void setSigungu(String sigungu) {
        this.sigungu = sigungu;
    }

    public String getEmdName() {
        return emdName;
    }

    public void setEmdName(String emdName) {
        this.emdName = emdName;
    }

    public String getEmdCode() {
        return emdCode;
    }

    public void setEmdCode(String emdCode) {
        this.emdCode = emdCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegionDTO)) {
            return false;
        }

        RegionDTO regionDTO = (RegionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, regionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RegionDTO{" +
            "id=" + getId() +
            ", sido='" + getSido() + "'" +
            ", sigungu='" + getSigungu() + "'" +
            ", emdName='" + getEmdName() + "'" +
            ", emdCode='" + getEmdCode() + "'" +
            "}";
    }
}
