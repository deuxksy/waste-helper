package com.waste.helper.service;

import com.waste.helper.service.dto.WasteImageDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.WasteImage}.
 */
public interface WasteImageService {
    /**
     * Save a wasteImage.
     *
     * @param wasteImageDTO the entity to save.
     * @return the persisted entity.
     */
    WasteImageDTO save(WasteImageDTO wasteImageDTO);

    /**
     * Updates a wasteImage.
     *
     * @param wasteImageDTO the entity to update.
     * @return the persisted entity.
     */
    WasteImageDTO update(WasteImageDTO wasteImageDTO);

    /**
     * Partially updates a wasteImage.
     *
     * @param wasteImageDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<WasteImageDTO> partialUpdate(WasteImageDTO wasteImageDTO);

    /**
     * Get all the wasteImages.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WasteImageDTO> findAll(Pageable pageable);

    /**
     * Get the "id" wasteImage.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<WasteImageDTO> findOne(Long id);

    /**
     * Delete the "id" wasteImage.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
