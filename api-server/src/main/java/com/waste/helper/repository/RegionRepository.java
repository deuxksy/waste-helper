package com.waste.helper.repository;

import com.waste.helper.domain.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Region entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByEmdCode(String emdCode);

    List<Region> findBySido(String sido);

    List<Region> findBySidoContainingOrSigunguContainingOrEmdNameContaining(
        String sido, String sigungu, String emdName
    );
}
