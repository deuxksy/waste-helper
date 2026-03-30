package com.waste.helper.service.impl;

import com.waste.helper.domain.WasteImage;
import com.waste.helper.repository.WasteImageRepository;
import com.waste.helper.service.WasteImageService;
import com.waste.helper.service.dto.WasteImageDTO;
import com.waste.helper.service.mapper.WasteImageMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.waste.helper.domain.WasteImage}.
 */
@Service
@Transactional
public class WasteImageServiceImpl implements WasteImageService {

    private static final Logger LOG = LoggerFactory.getLogger(WasteImageServiceImpl.class);

    private final WasteImageRepository wasteImageRepository;

    private final WasteImageMapper wasteImageMapper;

    public WasteImageServiceImpl(WasteImageRepository wasteImageRepository, WasteImageMapper wasteImageMapper) {
        this.wasteImageRepository = wasteImageRepository;
        this.wasteImageMapper = wasteImageMapper;
    }

    @Override
    public WasteImageDTO save(WasteImageDTO wasteImageDTO) {
        LOG.debug("Request to save WasteImage : {}", wasteImageDTO);
        WasteImage wasteImage = wasteImageMapper.toEntity(wasteImageDTO);
        wasteImage = wasteImageRepository.save(wasteImage);
        return wasteImageMapper.toDto(wasteImage);
    }

    @Override
    public WasteImageDTO update(WasteImageDTO wasteImageDTO) {
        LOG.debug("Request to update WasteImage : {}", wasteImageDTO);
        WasteImage wasteImage = wasteImageMapper.toEntity(wasteImageDTO);
        wasteImage = wasteImageRepository.save(wasteImage);
        return wasteImageMapper.toDto(wasteImage);
    }

    @Override
    public Optional<WasteImageDTO> partialUpdate(WasteImageDTO wasteImageDTO) {
        LOG.debug("Request to partially update WasteImage : {}", wasteImageDTO);

        return wasteImageRepository
            .findById(wasteImageDTO.getId())
            .map(existingWasteImage -> {
                wasteImageMapper.partialUpdate(existingWasteImage, wasteImageDTO);

                return existingWasteImage;
            })
            .map(wasteImageRepository::save)
            .map(wasteImageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WasteImageDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all WasteImages");
        return wasteImageRepository.findAll(pageable).map(wasteImageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WasteImageDTO> findOne(Long id) {
        LOG.debug("Request to get WasteImage : {}", id);
        return wasteImageRepository.findById(id).map(wasteImageMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete WasteImage : {}", id);
        wasteImageRepository.deleteById(id);
    }
}
