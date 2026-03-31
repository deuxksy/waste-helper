package com.waste.helper.web.rest;

import com.waste.helper.repository.NotificationSettingRepository;
import com.waste.helper.service.NotificationSettingService;
import com.waste.helper.service.dto.NotificationSettingDTO;
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
 * REST controller for managing {@link com.waste.helper.domain.NotificationSetting}.
 */
@RestController
@RequestMapping("/api/notification-settings")
public class NotificationSettingResource {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationSettingResource.class);

    private static final String ENTITY_NAME = "notificationSetting";

    @Value("${jhipster.clientApp.name:wasteHelper}")
    private String applicationName;

    private final NotificationSettingService notificationSettingService;

    private final NotificationSettingRepository notificationSettingRepository;

    public NotificationSettingResource(
        NotificationSettingService notificationSettingService,
        NotificationSettingRepository notificationSettingRepository
    ) {
        this.notificationSettingService = notificationSettingService;
        this.notificationSettingRepository = notificationSettingRepository;
    }

    /**
     * {@code POST  /notification-settings} : Create a new notificationSetting.
     *
     * @param notificationSettingDTO the notificationSettingDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new notificationSettingDTO, or with status {@code 400 (Bad Request)} if the notificationSetting has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NotificationSettingDTO> createNotificationSetting(@RequestBody NotificationSettingDTO notificationSettingDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save NotificationSetting : {}", notificationSettingDTO);
        if (notificationSettingDTO.getId() != null) {
            throw new BadRequestAlertException("A new notificationSetting cannot already have an ID", ENTITY_NAME, "idexists");
        }
        notificationSettingDTO = notificationSettingService.save(notificationSettingDTO);
        return ResponseEntity.created(new URI("/api/notification-settings/" + notificationSettingDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, notificationSettingDTO.getId().toString()))
            .body(notificationSettingDTO);
    }

    /**
     * {@code PUT  /notification-settings/:id} : Updates an existing notificationSetting.
     *
     * @param id the id of the notificationSettingDTO to save.
     * @param notificationSettingDTO the notificationSettingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated notificationSettingDTO,
     * or with status {@code 400 (Bad Request)} if the notificationSettingDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the notificationSettingDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NotificationSettingDTO> updateNotificationSetting(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody NotificationSettingDTO notificationSettingDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update NotificationSetting : {}, {}", id, notificationSettingDTO);
        if (notificationSettingDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, notificationSettingDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!notificationSettingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        notificationSettingDTO = notificationSettingService.update(notificationSettingDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, notificationSettingDTO.getId().toString()))
            .body(notificationSettingDTO);
    }

    /**
     * {@code PATCH  /notification-settings/:id} : Partial updates given fields of an existing notificationSetting, field will ignore if it is null
     *
     * @param id the id of the notificationSettingDTO to save.
     * @param notificationSettingDTO the notificationSettingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated notificationSettingDTO,
     * or with status {@code 400 (Bad Request)} if the notificationSettingDTO is not valid,
     * or with status {@code 404 (Not Found)} if the notificationSettingDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the notificationSettingDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NotificationSettingDTO> partialUpdateNotificationSetting(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody NotificationSettingDTO notificationSettingDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update NotificationSetting partially : {}, {}", id, notificationSettingDTO);
        if (notificationSettingDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, notificationSettingDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!notificationSettingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NotificationSettingDTO> result = notificationSettingService.partialUpdate(notificationSettingDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, notificationSettingDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /notification-settings} : get all the Notification Settings.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Notification Settings in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NotificationSettingDTO>> getAllNotificationSettings(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of NotificationSettings");
        Page<NotificationSettingDTO> page;
        if (eagerload) {
            page = notificationSettingService.findAllWithEagerRelationships(pageable);
        } else {
            page = notificationSettingService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /notification-settings/:id} : get the "id" notificationSetting.
     *
     * @param id the id of the notificationSettingDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the notificationSettingDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationSettingDTO> getNotificationSetting(@PathVariable("id") Long id) {
        LOG.debug("REST request to get NotificationSetting : {}", id);
        Optional<NotificationSettingDTO> notificationSettingDTO = notificationSettingService.findOne(id);
        return ResponseUtil.wrapOrNotFound(notificationSettingDTO);
    }

    /**
     * {@code DELETE  /notification-settings/:id} : delete the "id" notificationSetting.
     *
     * @param id the id of the notificationSettingDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotificationSetting(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete NotificationSetting : {}", id);
        notificationSettingService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
