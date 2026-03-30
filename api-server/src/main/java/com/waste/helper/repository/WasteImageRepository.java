package com.waste.helper.repository;

import com.waste.helper.domain.WasteImage;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WasteImage entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WasteImageRepository extends JpaRepository<WasteImage, Long> {}
