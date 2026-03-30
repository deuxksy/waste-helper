package com.waste.helper.service;

import com.waste.helper.service.dto.NotificationSettingDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.waste.helper.domain.NotificationSetting}.
 */
public interface NotificationSettingService {
    /**
     * Save a notificationSetting.
     *
     * @param notificationSettingDTO the entity to save.
     * @return the persisted entity.
     */
    NotificationSettingDTO save(NotificationSettingDTO notificationSettingDTO);

    /**
     * Updates a notificationSetting.
     *
     * @param notificationSettingDTO the entity to update.
     * @return the persisted entity.
     */
    NotificationSettingDTO update(NotificationSettingDTO notificationSettingDTO);

    /**
     * Partially updates a notificationSetting.
     *
     * @param notificationSettingDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<NotificationSettingDTO> partialUpdate(NotificationSettingDTO notificationSettingDTO);

    /**
     * Get all the notificationSettings.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NotificationSettingDTO> findAll(Pageable pageable);

    /**
     * Get all the notificationSettings with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NotificationSettingDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" notificationSetting.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<NotificationSettingDTO> findOne(Long id);

    /**
     * Delete the "id" notificationSetting.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
