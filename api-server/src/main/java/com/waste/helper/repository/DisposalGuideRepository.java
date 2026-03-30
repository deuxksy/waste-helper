package com.waste.helper.repository;

import com.waste.helper.domain.DisposalGuide;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DisposalGuide entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DisposalGuideRepository extends JpaRepository<DisposalGuide, Long> {}
