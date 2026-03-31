package com.waste.helper.service.impl;

import com.waste.helper.domain.DisposalGuide;
import com.waste.helper.repository.DisposalGuideRepository;
import com.waste.helper.service.DisposalGuideService;
import com.waste.helper.service.dto.DisposalGuideDTO;
import com.waste.helper.service.mapper.DisposalGuideMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.waste.helper.domain.DisposalGuide}.
 */
@Service
@Transactional
public class DisposalGuideServiceImpl implements DisposalGuideService {

    private static final Logger LOG = LoggerFactory.getLogger(DisposalGuideServiceImpl.class);

    private final DisposalGuideRepository disposalGuideRepository;

    private final DisposalGuideMapper disposalGuideMapper;

    public DisposalGuideServiceImpl(DisposalGuideRepository disposalGuideRepository, DisposalGuideMapper disposalGuideMapper) {
        this.disposalGuideRepository = disposalGuideRepository;
        this.disposalGuideMapper = disposalGuideMapper;
    }

    @Override
    public DisposalGuideDTO save(DisposalGuideDTO disposalGuideDTO) {
        LOG.debug("Request to save DisposalGuide : {}", disposalGuideDTO);
        DisposalGuide disposalGuide = disposalGuideMapper.toEntity(disposalGuideDTO);
        disposalGuide = disposalGuideRepository.save(disposalGuide);
        return disposalGuideMapper.toDto(disposalGuide);
    }

    @Override
    public DisposalGuideDTO update(DisposalGuideDTO disposalGuideDTO) {
        LOG.debug("Request to update DisposalGuide : {}", disposalGuideDTO);
        DisposalGuide disposalGuide = disposalGuideMapper.toEntity(disposalGuideDTO);
        disposalGuide = disposalGuideRepository.save(disposalGuide);
        return disposalGuideMapper.toDto(disposalGuide);
    }

    @Override
    public Optional<DisposalGuideDTO> partialUpdate(DisposalGuideDTO disposalGuideDTO) {
        LOG.debug("Request to partially update DisposalGuide : {}", disposalGuideDTO);

        return disposalGuideRepository
            .findById(disposalGuideDTO.getId())
            .map(existingDisposalGuide -> {
                disposalGuideMapper.partialUpdate(existingDisposalGuide, disposalGuideDTO);

                return existingDisposalGuide;
            })
            .map(disposalGuideRepository::save)
            .map(disposalGuideMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisposalGuideDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DisposalGuides");
        return disposalGuideRepository.findAll(pageable).map(disposalGuideMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DisposalGuideDTO> findOne(Long id) {
        LOG.debug("Request to get DisposalGuide : {}", id);
        return disposalGuideRepository.findById(id).map(disposalGuideMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DisposalGuide : {}", id);
        disposalGuideRepository.deleteById(id);
    }
}
