package com.waste.helper.service.impl;

import com.waste.helper.domain.NotificationSetting;
import com.waste.helper.repository.NotificationSettingRepository;
import com.waste.helper.service.NotificationSettingService;
import com.waste.helper.service.dto.NotificationSettingDTO;
import com.waste.helper.service.mapper.NotificationSettingMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.waste.helper.domain.NotificationSetting}.
 */
@Service
@Transactional
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationSettingServiceImpl.class);

    private final NotificationSettingRepository notificationSettingRepository;

    private final NotificationSettingMapper notificationSettingMapper;

    public NotificationSettingServiceImpl(
        NotificationSettingRepository notificationSettingRepository,
        NotificationSettingMapper notificationSettingMapper
    ) {
        this.notificationSettingRepository = notificationSettingRepository;
        this.notificationSettingMapper = notificationSettingMapper;
    }

    @Override
    public NotificationSettingDTO save(NotificationSettingDTO notificationSettingDTO) {
        LOG.debug("Request to save NotificationSetting : {}", notificationSettingDTO);
        NotificationSetting notificationSetting = notificationSettingMapper.toEntity(notificationSettingDTO);
        notificationSetting = notificationSettingRepository.save(notificationSetting);
        return notificationSettingMapper.toDto(notificationSetting);
    }

    @Override
    public NotificationSettingDTO update(NotificationSettingDTO notificationSettingDTO) {
        LOG.debug("Request to update NotificationSetting : {}", notificationSettingDTO);
        NotificationSetting notificationSetting = notificationSettingMapper.toEntity(notificationSettingDTO);
        notificationSetting = notificationSettingRepository.save(notificationSetting);
        return notificationSettingMapper.toDto(notificationSetting);
    }

    @Override
    public Optional<NotificationSettingDTO> partialUpdate(NotificationSettingDTO notificationSettingDTO) {
        LOG.debug("Request to partially update NotificationSetting : {}", notificationSettingDTO);

        return notificationSettingRepository
            .findById(notificationSettingDTO.getId())
            .map(existingNotificationSetting -> {
                notificationSettingMapper.partialUpdate(existingNotificationSetting, notificationSettingDTO);

                return existingNotificationSetting;
            })
            .map(notificationSettingRepository::save)
            .map(notificationSettingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationSettingDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all NotificationSettings");
        return notificationSettingRepository.findAll(pageable).map(notificationSettingMapper::toDto);
    }

    public Page<NotificationSettingDTO> findAllWithEagerRelationships(Pageable pageable) {
        return notificationSettingRepository.findAllWithEagerRelationships(pageable).map(notificationSettingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationSettingDTO> findOne(Long id) {
        LOG.debug("Request to get NotificationSetting : {}", id);
        return notificationSettingRepository.findOneWithEagerRelationships(id).map(notificationSettingMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete NotificationSetting : {}", id);
        notificationSettingRepository.deleteById(id);
    }
}
