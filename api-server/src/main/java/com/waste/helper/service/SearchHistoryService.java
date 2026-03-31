package com.waste.helper.service;

import com.waste.helper.service.dto.SearchHistoryDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.SearchHistory}.
 */
public interface SearchHistoryService {
    /**
     * Save a searchHistory.
     *
     * @param searchHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    SearchHistoryDTO save(SearchHistoryDTO searchHistoryDTO);

    /**
     * Updates a searchHistory.
     *
     * @param searchHistoryDTO the entity to update.
     * @return the persisted entity.
     */
    SearchHistoryDTO update(SearchHistoryDTO searchHistoryDTO);

    /**
     * Partially updates a searchHistory.
     *
     * @param searchHistoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SearchHistoryDTO> partialUpdate(SearchHistoryDTO searchHistoryDTO);

    /**
     * Get all the searchHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SearchHistoryDTO> findAll(Pageable pageable);

    /**
     * Get all the searchHistories with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SearchHistoryDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" searchHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SearchHistoryDTO> findOne(Long id);

    /**
     * Delete the "id" searchHistory.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
