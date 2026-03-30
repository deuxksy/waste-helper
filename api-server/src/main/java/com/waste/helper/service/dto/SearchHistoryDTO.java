package com.waste.helper.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.waste.helper.domain.SearchHistory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SearchHistoryDTO implements Serializable {

    private Long id;

    private String query;

    @Lob
    private String resultSummary;

    @NotNull
    private Instant classifiedAt;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
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
        if (!(o instanceof SearchHistoryDTO)) {
            return false;
        }

        SearchHistoryDTO searchHistoryDTO = (SearchHistoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, searchHistoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SearchHistoryDTO{" +
            "id=" + getId() +
            ", query='" + getQuery() + "'" +
            ", resultSummary='" + getResultSummary() + "'" +
            ", classifiedAt='" + getClassifiedAt() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
