package com.waste.helper.service;

import com.waste.helper.service.dto.DisposalGuideDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.DisposalGuide}.
 */
public interface DisposalGuideService {
    /**
     * Save a disposalGuide.
     *
     * @param disposalGuideDTO the entity to save.
     * @return the persisted entity.
     */
    DisposalGuideDTO save(DisposalGuideDTO disposalGuideDTO);

    /**
     * Updates a disposalGuide.
     *
     * @param disposalGuideDTO the entity to update.
     * @return the persisted entity.
     */
    DisposalGuideDTO update(DisposalGuideDTO disposalGuideDTO);

    /**
     * Partially updates a disposalGuide.
     *
     * @param disposalGuideDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DisposalGuideDTO> partialUpdate(DisposalGuideDTO disposalGuideDTO);

    /**
     * Get all the disposalGuides.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DisposalGuideDTO> findAll(Pageable pageable);

    /**
     * Get the "id" disposalGuide.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DisposalGuideDTO> findOne(Long id);

    /**
     * Delete the "id" disposalGuide.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
