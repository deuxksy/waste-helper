package com.waste.helper.service;

import com.waste.helper.service.dto.FeedbackDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.Feedback}.
 */
public interface FeedbackService {
    /**
     * Save a feedback.
     *
     * @param feedbackDTO the entity to save.
     * @return the persisted entity.
     */
    FeedbackDTO save(FeedbackDTO feedbackDTO);

    /**
     * Updates a feedback.
     *
     * @param feedbackDTO the entity to update.
     * @return the persisted entity.
     */
    FeedbackDTO update(FeedbackDTO feedbackDTO);

    /**
     * Partially updates a feedback.
     *
     * @param feedbackDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FeedbackDTO> partialUpdate(FeedbackDTO feedbackDTO);

    /**
     * Get all the feedbacks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FeedbackDTO> findAll(Pageable pageable);

    /**
     * Get all the feedbacks with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FeedbackDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" feedback.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FeedbackDTO> findOne(Long id);

    /**
     * Delete the "id" feedback.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
