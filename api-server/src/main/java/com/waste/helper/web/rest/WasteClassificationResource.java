package com.waste.helper.web.rest;

import com.waste.helper.repository.WasteClassificationRepository;
import com.waste.helper.service.WasteClassificationService;
import com.waste.helper.service.dto.WasteClassificationDTO;
import com.waste.helper.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * REST controller for managing {@link com.waste.helper.domain.WasteClassification}.
 */
@RestController
@RequestMapping("/api/waste-classifications")
public class WasteClassificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(WasteClassificationResource.class);

    private static final String ENTITY_NAME = "wasteClassification";

    @Value("${jhipster.clientApp.name:wasteHelper}")
    private String applicationName;

    private final WasteClassificationService wasteClassificationService;

    private final WasteClassificationRepository wasteClassificationRepository;

    public WasteClassificationResource(
        WasteClassificationService wasteClassificationService,
        WasteClassificationRepository wasteClassificationRepository
    ) {
        this.wasteClassificationService = wasteClassificationService;
        this.wasteClassificationRepository = wasteClassificationRepository;
    }

    /**
     * {@code POST  /waste-classifications} : Create a new wasteClassification.
     *
     * @param wasteClassificationDTO the wasteClassificationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new wasteClassificationDTO, or with status {@code 400 (Bad Request)} if the wasteClassification has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<WasteClassificationDTO> createWasteClassification(
        @Valid @RequestBody WasteClassificationDTO wasteClassificationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save WasteClassification : {}", wasteClassificationDTO);
        if (wasteClassificationDTO.getId() != null) {
            throw new BadRequestAlertException("A new wasteClassification cannot already have an ID", ENTITY_NAME, "idexists");
        }
        wasteClassificationDTO = wasteClassificationService.save(wasteClassificationDTO);
        return ResponseEntity.created(new URI("/api/waste-classifications/" + wasteClassificationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, wasteClassificationDTO.getId().toString()))
            .body(wasteClassificationDTO);
    }

    /**
     * {@code PUT  /waste-classifications/:id} : Updates an existing wasteClassification.
     *
     * @param id the id of the wasteClassificationDTO to save.
     * @param wasteClassificationDTO the wasteClassificationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wasteClassificationDTO,
     * or with status {@code 400 (Bad Request)} if the wasteClassificationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the wasteClassificationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WasteClassificationDTO> updateWasteClassification(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody WasteClassificationDTO wasteClassificationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update WasteClassification : {}, {}", id, wasteClassificationDTO);
        if (wasteClassificationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wasteClassificationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wasteClassificationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        wasteClassificationDTO = wasteClassificationService.update(wasteClassificationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wasteClassificationDTO.getId().toString()))
            .body(wasteClassificationDTO);
    }

    /**
     * {@code PATCH  /waste-classifications/:id} : Partial updates given fields of an existing wasteClassification, field will ignore if it is null
     *
     * @param id the id of the wasteClassificationDTO to save.
     * @param wasteClassificationDTO the wasteClassificationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wasteClassificationDTO,
     * or with status {@code 400 (Bad Request)} if the wasteClassificationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the wasteClassificationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the wasteClassificationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WasteClassificationDTO> partialUpdateWasteClassification(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody WasteClassificationDTO wasteClassificationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update WasteClassification partially : {}, {}", id, wasteClassificationDTO);
        if (wasteClassificationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wasteClassificationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wasteClassificationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WasteClassificationDTO> result = wasteClassificationService.partialUpdate(wasteClassificationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wasteClassificationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /waste-classifications} : get all the Waste Classifications.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Waste Classifications in body.
     */
    @GetMapping("")
    public ResponseEntity<List<WasteClassificationDTO>> getAllWasteClassifications(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of WasteClassifications");
        Page<WasteClassificationDTO> page;
        if (eagerload) {
            page = wasteClassificationService.findAllWithEagerRelationships(pageable);
        } else {
            page = wasteClassificationService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /waste-classifications/:id} : get the "id" wasteClassification.
     *
     * @param id the id of the wasteClassificationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the wasteClassificationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WasteClassificationDTO> getWasteClassification(@PathVariable("id") Long id) {
        LOG.debug("REST request to get WasteClassification : {}", id);
        Optional<WasteClassificationDTO> wasteClassificationDTO = wasteClassificationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(wasteClassificationDTO);
    }

    /**
     * {@code DELETE  /waste-classifications/:id} : delete the "id" wasteClassification.
     *
     * @param id the id of the wasteClassificationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWasteClassification(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete WasteClassification : {}", id);
        wasteClassificationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
