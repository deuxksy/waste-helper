package com.waste.helper.service.impl;

import com.waste.helper.domain.WasteClassification;
import com.waste.helper.repository.WasteClassificationRepository;
import com.waste.helper.service.WasteClassificationService;
import com.waste.helper.service.dto.WasteClassificationDTO;
import com.waste.helper.service.mapper.WasteClassificationMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.waste.helper.domain.WasteClassification}.
 */
@Service
@Transactional
public class WasteClassificationServiceImpl implements WasteClassificationService {

    private static final Logger LOG = LoggerFactory.getLogger(WasteClassificationServiceImpl.class);

    private final WasteClassificationRepository wasteClassificationRepository;

    private final WasteClassificationMapper wasteClassificationMapper;

    public WasteClassificationServiceImpl(
        WasteClassificationRepository wasteClassificationRepository,
        WasteClassificationMapper wasteClassificationMapper
    ) {
        this.wasteClassificationRepository = wasteClassificationRepository;
        this.wasteClassificationMapper = wasteClassificationMapper;
    }

    @Override
    public WasteClassificationDTO save(WasteClassificationDTO wasteClassificationDTO) {
        LOG.debug("Request to save WasteClassification : {}", wasteClassificationDTO);
        WasteClassification wasteClassification = wasteClassificationMapper.toEntity(wasteClassificationDTO);
        wasteClassification = wasteClassificationRepository.save(wasteClassification);
        return wasteClassificationMapper.toDto(wasteClassification);
    }

    @Override
    public WasteClassificationDTO update(WasteClassificationDTO wasteClassificationDTO) {
        LOG.debug("Request to update WasteClassification : {}", wasteClassificationDTO);
        WasteClassification wasteClassification = wasteClassificationMapper.toEntity(wasteClassificationDTO);
        wasteClassification = wasteClassificationRepository.save(wasteClassification);
        return wasteClassificationMapper.toDto(wasteClassification);
    }

    @Override
    public Optional<WasteClassificationDTO> partialUpdate(WasteClassificationDTO wasteClassificationDTO) {
        LOG.debug("Request to partially update WasteClassification : {}", wasteClassificationDTO);

        return wasteClassificationRepository
            .findById(wasteClassificationDTO.getId())
            .map(existingWasteClassification -> {
                wasteClassificationMapper.partialUpdate(existingWasteClassification, wasteClassificationDTO);

                return existingWasteClassification;
            })
            .map(wasteClassificationRepository::save)
            .map(wasteClassificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WasteClassificationDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all WasteClassifications");
        return wasteClassificationRepository.findAll(pageable).map(wasteClassificationMapper::toDto);
    }

    public Page<WasteClassificationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return wasteClassificationRepository.findAllWithEagerRelationships(pageable).map(wasteClassificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WasteClassificationDTO> findOne(Long id) {
        LOG.debug("Request to get WasteClassification : {}", id);
        return wasteClassificationRepository.findOneWithEagerRelationships(id).map(wasteClassificationMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete WasteClassification : {}", id);
        wasteClassificationRepository.deleteById(id);
    }
}
