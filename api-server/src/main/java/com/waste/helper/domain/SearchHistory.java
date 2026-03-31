package com.waste.helper.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A SearchHistory.
 */
@Entity
@Table(name = "search_history")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SearchHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "query")
    private String query;

    @Lob
    @Column(name = "result_summary")
    private String resultSummary;

    @NotNull
    @Column(name = "classified_at", nullable = false)
    private Instant classifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SearchHistory id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return this.query;
    }

    public SearchHistory query(String query) {
        this.setQuery(query);
        return this;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResultSummary() {
        return this.resultSummary;
    }

    public SearchHistory resultSummary(String resultSummary) {
        this.setResultSummary(resultSummary);
        return this;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public Instant getClassifiedAt() {
        return this.classifiedAt;
    }

    public SearchHistory classifiedAt(Instant classifiedAt) {
        this.setClassifiedAt(classifiedAt);
        return this;
    }

    public void setClassifiedAt(Instant classifiedAt) {
        this.classifiedAt = classifiedAt;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SearchHistory user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchHistory)) {
            return false;
        }
        return getId() != null && getId().equals(((SearchHistory) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SearchHistory{" +
            "id=" + getId() +
            ", query='" + getQuery() + "'" +
            ", resultSummary='" + getResultSummary() + "'" +
            ", classifiedAt='" + getClassifiedAt() + "'" +
            "}";
    }
}
