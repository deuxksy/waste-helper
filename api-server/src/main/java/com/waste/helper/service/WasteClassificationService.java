package com.waste.helper.service;

import com.waste.helper.service.dto.WasteClassificationDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.WasteClassification}.
 */
public interface WasteClassificationService {
    /**
     * Save a wasteClassification.
     *
     * @param wasteClassificationDTO the entity to save.
     * @return the persisted entity.
     */
    WasteClassificationDTO save(WasteClassificationDTO wasteClassificationDTO);

    /**
     * Updates a wasteClassification.
     *
     * @param wasteClassificationDTO the entity to update.
     * @return the persisted entity.
     */
    WasteClassificationDTO update(WasteClassificationDTO wasteClassificationDTO);

    /**
     * Partially updates a wasteClassification.
     *
     * @param wasteClassificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<WasteClassificationDTO> partialUpdate(WasteClassificationDTO wasteClassificationDTO);

    /**
     * Get all the wasteClassifications.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WasteClassificationDTO> findAll(Pageable pageable);

    /**
     * Get all the wasteClassifications with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WasteClassificationDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" wasteClassification.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<WasteClassificationDTO> findOne(Long id);

    /**
     * Delete the "id" wasteClassification.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
