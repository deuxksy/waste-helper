package com.waste.helper.web.rest;

import com.waste.helper.repository.DisposalGuideRepository;
import com.waste.helper.service.DisposalGuideService;
import com.waste.helper.service.dto.DisposalGuideDTO;
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
 * REST controller for managing {@link com.waste.helper.domain.DisposalGuide}.
 */
@RestController
@RequestMapping("/api/disposal-guides")
public class DisposalGuideResource {

    private static final Logger LOG = LoggerFactory.getLogger(DisposalGuideResource.class);

    private static final String ENTITY_NAME = "disposalGuide";

    @Value("${jhipster.clientApp.name:wasteHelper}")
    private String applicationName;

    private final DisposalGuideService disposalGuideService;

    private final DisposalGuideRepository disposalGuideRepository;

    public DisposalGuideResource(DisposalGuideService disposalGuideService, DisposalGuideRepository disposalGuideRepository) {
        this.disposalGuideService = disposalGuideService;
        this.disposalGuideRepository = disposalGuideRepository;
    }

    /**
     * {@code POST  /disposal-guides} : Create a new disposalGuide.
     *
     * @param disposalGuideDTO the disposalGuideDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new disposalGuideDTO, or with status {@code 400 (Bad Request)} if the disposalGuide has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DisposalGuideDTO> createDisposalGuide(@Valid @RequestBody DisposalGuideDTO disposalGuideDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DisposalGuide : {}", disposalGuideDTO);
        if (disposalGuideDTO.getId() != null) {
            throw new BadRequestAlertException("A new disposalGuide cannot already have an ID", ENTITY_NAME, "idexists");
        }
        disposalGuideDTO = disposalGuideService.save(disposalGuideDTO);
        return ResponseEntity.created(new URI("/api/disposal-guides/" + disposalGuideDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, disposalGuideDTO.getId().toString()))
            .body(disposalGuideDTO);
    }

    /**
     * {@code PUT  /disposal-guides/:id} : Updates an existing disposalGuide.
     *
     * @param id the id of the disposalGuideDTO to save.
     * @param disposalGuideDTO the disposalGuideDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated disposalGuideDTO,
     * or with status {@code 400 (Bad Request)} if the disposalGuideDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the disposalGuideDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DisposalGuideDTO> updateDisposalGuide(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DisposalGuideDTO disposalGuideDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DisposalGuide : {}, {}", id, disposalGuideDTO);
        if (disposalGuideDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, disposalGuideDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!disposalGuideRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        disposalGuideDTO = disposalGuideService.update(disposalGuideDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, disposalGuideDTO.getId().toString()))
            .body(disposalGuideDTO);
    }

    /**
     * {@code PATCH  /disposal-guides/:id} : Partial updates given fields of an existing disposalGuide, field will ignore if it is null
     *
     * @param id the id of the disposalGuideDTO to save.
     * @param disposalGuideDTO the disposalGuideDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated disposalGuideDTO,
     * or with status {@code 400 (Bad Request)} if the disposalGuideDTO is not valid,
     * or with status {@code 404 (Not Found)} if the disposalGuideDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the disposalGuideDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DisposalGuideDTO> partialUpdateDisposalGuide(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DisposalGuideDTO disposalGuideDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DisposalGuide partially : {}, {}", id, disposalGuideDTO);
        if (disposalGuideDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, disposalGuideDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!disposalGuideRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DisposalGuideDTO> result = disposalGuideService.partialUpdate(disposalGuideDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, disposalGuideDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /disposal-guides} : get all the Disposal Guides.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Disposal Guides in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DisposalGuideDTO>> getAllDisposalGuides(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of DisposalGuides");
        Page<DisposalGuideDTO> page = disposalGuideService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /disposal-guides/:id} : get the "id" disposalGuide.
     *
     * @param id the id of the disposalGuideDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the disposalGuideDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DisposalGuideDTO> getDisposalGuide(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DisposalGuide : {}", id);
        Optional<DisposalGuideDTO> disposalGuideDTO = disposalGuideService.findOne(id);
        return ResponseUtil.wrapOrNotFound(disposalGuideDTO);
    }

    /**
     * {@code DELETE  /disposal-guides/:id} : delete the "id" disposalGuide.
     *
     * @param id the id of the disposalGuideDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisposalGuide(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DisposalGuide : {}", id);
        disposalGuideService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
