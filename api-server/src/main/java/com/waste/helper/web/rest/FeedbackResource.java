package com.waste.helper.web.rest;

import com.waste.helper.repository.FeedbackRepository;
import com.waste.helper.service.FeedbackService;
import com.waste.helper.service.dto.FeedbackDTO;
import com.waste.helper.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.waste.helper.domain.Feedback}.
 */
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackResource {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackResource.class);

    private static final String ENTITY_NAME = "feedback";

    @Value("${jhipster.clientApp.name:wasteHelper}")
    private String applicationName;

    private final FeedbackService feedbackService;

    private final FeedbackRepository feedbackRepository;

    public FeedbackResource(FeedbackService feedbackService, FeedbackRepository feedbackRepository) {
        this.feedbackService = feedbackService;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * {@code POST  /feedbacks} : Create a new feedback.
     *
     * @param feedbackDTO the feedbackDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new feedbackDTO, or with status {@code 400 (Bad Request)} if the feedback has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FeedbackDTO> createFeedback(@RequestBody FeedbackDTO feedbackDTO) throws URISyntaxException {
        LOG.debug("REST request to save Feedback : {}", feedbackDTO);
        if (feedbackDTO.getId() != null) {
            throw new BadRequestAlertException("A new feedback cannot already have an ID", ENTITY_NAME, "idexists");
        }
        feedbackDTO = feedbackService.save(feedbackDTO);
        return ResponseEntity.created(new URI("/api/feedbacks/" + feedbackDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, feedbackDTO.getId().toString()))
            .body(feedbackDTO);
    }

    /**
     * {@code PUT  /feedbacks/:id} : Updates an existing feedback.
     *
     * @param id the id of the feedbackDTO to save.
     * @param feedbackDTO the feedbackDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated feedbackDTO,
     * or with status {@code 400 (Bad Request)} if the feedbackDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the feedbackDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackDTO> updateFeedback(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody FeedbackDTO feedbackDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Feedback : {}, {}", id, feedbackDTO);
        if (feedbackDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, feedbackDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!feedbackRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        feedbackDTO = feedbackService.update(feedbackDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, feedbackDTO.getId().toString()))
            .body(feedbackDTO);
    }

    /**
     * {@code PATCH  /feedbacks/:id} : Partial updates given fields of an existing feedback, field will ignore if it is null
     *
     * @param id the id of the feedbackDTO to save.
     * @param feedbackDTO the feedbackDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated feedbackDTO,
     * or with status {@code 400 (Bad Request)} if the feedbackDTO is not valid,
     * or with status {@code 404 (Not Found)} if the feedbackDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the feedbackDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FeedbackDTO> partialUpdateFeedback(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody FeedbackDTO feedbackDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Feedback partially : {}, {}", id, feedbackDTO);
        if (feedbackDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, feedbackDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!feedbackRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FeedbackDTO> result = feedbackService.partialUpdate(feedbackDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, feedbackDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /feedbacks} : get all the Feedbacks.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Feedbacks in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbacks(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Feedbacks");
        Page<FeedbackDTO> page;
        if (eagerload) {
            page = feedbackService.findAllWithEagerRelationships(pageable);
        } else {
            page = feedbackService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /feedbacks/:id} : get the "id" feedback.
     *
     * @param id the id of the feedbackDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the feedbackDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getFeedback(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Feedback : {}", id);
        Optional<FeedbackDTO> feedbackDTO = feedbackService.findOne(id);
        return ResponseUtil.wrapOrNotFound(feedbackDTO);
    }

    /**
     * {@code DELETE  /feedbacks/:id} : delete the "id" feedback.
     *
     * @param id the id of the feedbackDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Feedback : {}", id);
        feedbackService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
