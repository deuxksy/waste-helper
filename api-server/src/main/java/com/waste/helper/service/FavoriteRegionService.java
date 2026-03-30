package com.waste.helper.service;

import com.waste.helper.service.dto.FavoriteRegionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.FavoriteRegion}.
 */
public interface FavoriteRegionService {
    /**
     * Save a favoriteRegion.
     *
     * @param favoriteRegionDTO the entity to save.
     * @return the persisted entity.
     */
    FavoriteRegionDTO save(FavoriteRegionDTO favoriteRegionDTO);

    /**
     * Updates a favoriteRegion.
     *
     * @param favoriteRegionDTO the entity to update.
     * @return the persisted entity.
     */
    FavoriteRegionDTO update(FavoriteRegionDTO favoriteRegionDTO);

    /**
     * Partially updates a favoriteRegion.
     *
     * @param favoriteRegionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FavoriteRegionDTO> partialUpdate(FavoriteRegionDTO favoriteRegionDTO);

    /**
     * Get all the favoriteRegions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FavoriteRegionDTO> findAll(Pageable pageable);

    /**
     * Get all the favoriteRegions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FavoriteRegionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" favoriteRegion.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FavoriteRegionDTO> findOne(Long id);

    /**
     * Delete the "id" favoriteRegion.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
