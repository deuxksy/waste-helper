package com.waste.helper.web.rest;

import com.waste.helper.repository.FavoriteRegionRepository;
import com.waste.helper.service.FavoriteRegionService;
import com.waste.helper.service.dto.FavoriteRegionDTO;
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
 * REST controller for managing {@link com.waste.helper.domain.FavoriteRegion}.
 */
@RestController
@RequestMapping("/api/favorite-regions")
public class FavoriteRegionResource {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteRegionResource.class);

    private static final String ENTITY_NAME = "favoriteRegion";

    @Value("${jhipster.clientApp.name:wasteHelper}")
    private String applicationName;

    private final FavoriteRegionService favoriteRegionService;

    private final FavoriteRegionRepository favoriteRegionRepository;

    public FavoriteRegionResource(FavoriteRegionService favoriteRegionService, FavoriteRegionRepository favoriteRegionRepository) {
        this.favoriteRegionService = favoriteRegionService;
        this.favoriteRegionRepository = favoriteRegionRepository;
    }

    /**
     * {@code POST  /favorite-regions} : Create a new favoriteRegion.
     *
     * @param favoriteRegionDTO the favoriteRegionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new favoriteRegionDTO, or with status {@code 400 (Bad Request)} if the favoriteRegion has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FavoriteRegionDTO> createFavoriteRegion(@RequestBody FavoriteRegionDTO favoriteRegionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save FavoriteRegion : {}", favoriteRegionDTO);
        if (favoriteRegionDTO.getId() != null) {
            throw new BadRequestAlertException("A new favoriteRegion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        favoriteRegionDTO = favoriteRegionService.save(favoriteRegionDTO);
        return ResponseEntity.created(new URI("/api/favorite-regions/" + favoriteRegionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, favoriteRegionDTO.getId().toString()))
            .body(favoriteRegionDTO);
    }

    /**
     * {@code PUT  /favorite-regions/:id} : Updates an existing favoriteRegion.
     *
     * @param id the id of the favoriteRegionDTO to save.
     * @param favoriteRegionDTO the favoriteRegionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favoriteRegionDTO,
     * or with status {@code 400 (Bad Request)} if the favoriteRegionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the favoriteRegionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FavoriteRegionDTO> updateFavoriteRegion(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody FavoriteRegionDTO favoriteRegionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update FavoriteRegion : {}, {}", id, favoriteRegionDTO);
        if (favoriteRegionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, favoriteRegionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!favoriteRegionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        favoriteRegionDTO = favoriteRegionService.update(favoriteRegionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, favoriteRegionDTO.getId().toString()))
            .body(favoriteRegionDTO);
    }

    /**
     * {@code PATCH  /favorite-regions/:id} : Partial updates given fields of an existing favoriteRegion, field will ignore if it is null
     *
     * @param id the id of the favoriteRegionDTO to save.
     * @param favoriteRegionDTO the favoriteRegionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favoriteRegionDTO,
     * or with status {@code 400 (Bad Request)} if the favoriteRegionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the favoriteRegionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the favoriteRegionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FavoriteRegionDTO> partialUpdateFavoriteRegion(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody FavoriteRegionDTO favoriteRegionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update FavoriteRegion partially : {}, {}", id, favoriteRegionDTO);
        if (favoriteRegionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, favoriteRegionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!favoriteRegionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FavoriteRegionDTO> result = favoriteRegionService.partialUpdate(favoriteRegionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, favoriteRegionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /favorite-regions} : get all the Favorite Regions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Favorite Regions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FavoriteRegionDTO>> getAllFavoriteRegions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of FavoriteRegions");
        Page<FavoriteRegionDTO> page;
        if (eagerload) {
            page = favoriteRegionService.findAllWithEagerRelationships(pageable);
        } else {
            page = favoriteRegionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /favorite-regions/:id} : get the "id" favoriteRegion.
     *
     * @param id the id of the favoriteRegionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the favoriteRegionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FavoriteRegionDTO> getFavoriteRegion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get FavoriteRegion : {}", id);
        Optional<FavoriteRegionDTO> favoriteRegionDTO = favoriteRegionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(favoriteRegionDTO);
    }

    /**
     * {@code DELETE  /favorite-regions/:id} : delete the "id" favoriteRegion.
     *
     * @param id the id of the favoriteRegionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavoriteRegion(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete FavoriteRegion : {}", id);
        favoriteRegionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
