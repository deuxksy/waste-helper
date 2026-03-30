package com.waste.helper.repository;

import com.waste.helper.domain.FavoriteRegion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the FavoriteRegion entity.
 */
@Repository
public interface FavoriteRegionRepository extends JpaRepository<FavoriteRegion, Long> {
    @Query("select favoriteRegion from FavoriteRegion favoriteRegion where favoriteRegion.user.login = ?#{authentication.name}")
    List<FavoriteRegion> findByUserIsCurrentUser();

    default Optional<FavoriteRegion> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<FavoriteRegion> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<FavoriteRegion> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select favoriteRegion from FavoriteRegion favoriteRegion left join fetch favoriteRegion.user left join fetch favoriteRegion.region",
        countQuery = "select count(favoriteRegion) from FavoriteRegion favoriteRegion"
    )
    Page<FavoriteRegion> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select favoriteRegion from FavoriteRegion favoriteRegion left join fetch favoriteRegion.user left join fetch favoriteRegion.region"
    )
    List<FavoriteRegion> findAllWithToOneRelationships();

    @Query(
        "select favoriteRegion from FavoriteRegion favoriteRegion left join fetch favoriteRegion.user left join fetch favoriteRegion.region where favoriteRegion.id =:id"
    )
    Optional<FavoriteRegion> findOneWithToOneRelationships(@Param("id") Long id);
}
