package com.waste.helper.repository;

import com.waste.helper.domain.WasteClassification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WasteClassification entity.
 */
@Repository
public interface WasteClassificationRepository extends JpaRepository<WasteClassification, Long> {
    @Query(
        "select wasteClassification from WasteClassification wasteClassification where wasteClassification.user.login = ?#{authentication.name}"
    )
    List<WasteClassification> findByUserIsCurrentUser();

    default Optional<WasteClassification> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<WasteClassification> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<WasteClassification> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select wasteClassification from WasteClassification wasteClassification left join fetch wasteClassification.user",
        countQuery = "select count(wasteClassification) from WasteClassification wasteClassification"
    )
    Page<WasteClassification> findAllWithToOneRelationships(Pageable pageable);

    @Query("select wasteClassification from WasteClassification wasteClassification left join fetch wasteClassification.user")
    List<WasteClassification> findAllWithToOneRelationships();

    @Query(
        "select wasteClassification from WasteClassification wasteClassification left join fetch wasteClassification.user where wasteClassification.id =:id"
    )
    Optional<WasteClassification> findOneWithToOneRelationships(@Param("id") Long id);
}
