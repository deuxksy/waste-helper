package com.waste.helper.web.rest;

import com.waste.helper.repository.WasteImageRepository;
import com.waste.helper.service.WasteImageService;
import com.waste.helper.service.dto.WasteImageDTO;
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
 * REST controller for managing {@link com.waste.helper.domain.WasteImage}.
 */
@RestController
@RequestMapping("/api/waste-images")
public class WasteImageResource {

    private static final Logger LOG = LoggerFactory.getLogger(WasteImageResource.class);

    private static final String ENTITY_NAME = "wasteImage";

    @Value("${jhipster.clientApp.name:wasteHelper}")
    private String applicationName;

    private final WasteImageService wasteImageService;

    private final WasteImageRepository wasteImageRepository;

    public WasteImageResource(WasteImageService wasteImageService, WasteImageRepository wasteImageRepository) {
        this.wasteImageService = wasteImageService;
        this.wasteImageRepository = wasteImageRepository;
    }

    /**
     * {@code POST  /waste-images} : Create a new wasteImage.
     *
     * @param wasteImageDTO the wasteImageDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new wasteImageDTO, or with status {@code 400 (Bad Request)} if the wasteImage has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<WasteImageDTO> createWasteImage(@RequestBody WasteImageDTO wasteImageDTO) throws URISyntaxException {
        LOG.debug("REST request to save WasteImage : {}", wasteImageDTO);
        if (wasteImageDTO.getId() != null) {
            throw new BadRequestAlertException("A new wasteImage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        wasteImageDTO = wasteImageService.save(wasteImageDTO);
        return ResponseEntity.created(new URI("/api/waste-images/" + wasteImageDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, wasteImageDTO.getId().toString()))
            .body(wasteImageDTO);
    }

    /**
     * {@code PUT  /waste-images/:id} : Updates an existing wasteImage.
     *
     * @param id the id of the wasteImageDTO to save.
     * @param wasteImageDTO the wasteImageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wasteImageDTO,
     * or with status {@code 400 (Bad Request)} if the wasteImageDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the wasteImageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WasteImageDTO> updateWasteImage(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody WasteImageDTO wasteImageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update WasteImage : {}, {}", id, wasteImageDTO);
        if (wasteImageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wasteImageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wasteImageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        wasteImageDTO = wasteImageService.update(wasteImageDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wasteImageDTO.getId().toString()))
            .body(wasteImageDTO);
    }

    /**
     * {@code PATCH  /waste-images/:id} : Partial updates given fields of an existing wasteImage, field will ignore if it is null
     *
     * @param id the id of the wasteImageDTO to save.
     * @param wasteImageDTO the wasteImageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wasteImageDTO,
     * or with status {@code 400 (Bad Request)} if the wasteImageDTO is not valid,
     * or with status {@code 404 (Not Found)} if the wasteImageDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the wasteImageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WasteImageDTO> partialUpdateWasteImage(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody WasteImageDTO wasteImageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update WasteImage partially : {}, {}", id, wasteImageDTO);
        if (wasteImageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wasteImageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wasteImageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WasteImageDTO> result = wasteImageService.partialUpdate(wasteImageDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wasteImageDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /waste-images} : get all the Waste Images.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Waste Images in body.
     */
    @GetMapping("")
    public ResponseEntity<List<WasteImageDTO>> getAllWasteImages(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of WasteImages");
        Page<WasteImageDTO> page = wasteImageService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /waste-images/:id} : get the "id" wasteImage.
     *
     * @param id the id of the wasteImageDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the wasteImageDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WasteImageDTO> getWasteImage(@PathVariable("id") Long id) {
        LOG.debug("REST request to get WasteImage : {}", id);
        Optional<WasteImageDTO> wasteImageDTO = wasteImageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(wasteImageDTO);
    }

    /**
     * {@code DELETE  /waste-images/:id} : delete the "id" wasteImage.
     *
     * @param id the id of the wasteImageDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWasteImage(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete WasteImage : {}", id);
        wasteImageService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
