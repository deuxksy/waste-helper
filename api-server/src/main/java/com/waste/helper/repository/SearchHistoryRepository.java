package com.waste.helper.repository;

import com.waste.helper.domain.SearchHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SearchHistory entity.
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    @Query("select searchHistory from SearchHistory searchHistory where searchHistory.user.login = ?#{authentication.name}")
    List<SearchHistory> findByUserIsCurrentUser();

    default Optional<SearchHistory> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SearchHistory> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SearchHistory> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select searchHistory from SearchHistory searchHistory left join fetch searchHistory.user",
        countQuery = "select count(searchHistory) from SearchHistory searchHistory"
    )
    Page<SearchHistory> findAllWithToOneRelationships(Pageable pageable);

    @Query("select searchHistory from SearchHistory searchHistory left join fetch searchHistory.user")
    List<SearchHistory> findAllWithToOneRelationships();

    @Query("select searchHistory from SearchHistory searchHistory left join fetch searchHistory.user where searchHistory.id =:id")
    Optional<SearchHistory> findOneWithToOneRelationships(@Param("id") Long id);
}
