package com.waste.helper.repository;

import com.waste.helper.domain.DisposalGuide;
import com.waste.helper.domain.enumeration.Source;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DisposalGuide entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DisposalGuideRepository extends JpaRepository<DisposalGuide, Long> {

    Optional<DisposalGuide> findByRegion_EmdCodeAndWasteTypeAndSource(
        String emdCode, String wasteType, Source source
    );

    List<DisposalGuide> findByWasteType(String wasteType);
}
