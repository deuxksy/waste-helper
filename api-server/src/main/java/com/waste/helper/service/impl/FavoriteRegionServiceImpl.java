package com.waste.helper.service.impl;

import com.waste.helper.domain.FavoriteRegion;
import com.waste.helper.repository.FavoriteRegionRepository;
import com.waste.helper.service.FavoriteRegionService;
import com.waste.helper.service.dto.FavoriteRegionDTO;
import com.waste.helper.service.mapper.FavoriteRegionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.waste.helper.domain.FavoriteRegion}.
 */
@Service
@Transactional
public class FavoriteRegionServiceImpl implements FavoriteRegionService {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteRegionServiceImpl.class);

    private final FavoriteRegionRepository favoriteRegionRepository;

    private final FavoriteRegionMapper favoriteRegionMapper;

    public FavoriteRegionServiceImpl(FavoriteRegionRepository favoriteRegionRepository, FavoriteRegionMapper favoriteRegionMapper) {
        this.favoriteRegionRepository = favoriteRegionRepository;
        this.favoriteRegionMapper = favoriteRegionMapper;
    }

    @Override
    public FavoriteRegionDTO save(FavoriteRegionDTO favoriteRegionDTO) {
        LOG.debug("Request to save FavoriteRegion : {}", favoriteRegionDTO);
        FavoriteRegion favoriteRegion = favoriteRegionMapper.toEntity(favoriteRegionDTO);
        favoriteRegion = favoriteRegionRepository.save(favoriteRegion);
        return favoriteRegionMapper.toDto(favoriteRegion);
    }

    @Override
    public FavoriteRegionDTO update(FavoriteRegionDTO favoriteRegionDTO) {
        LOG.debug("Request to update FavoriteRegion : {}", favoriteRegionDTO);
        FavoriteRegion favoriteRegion = favoriteRegionMapper.toEntity(favoriteRegionDTO);
        favoriteRegion = favoriteRegionRepository.save(favoriteRegion);
        return favoriteRegionMapper.toDto(favoriteRegion);
    }

    @Override
    public Optional<FavoriteRegionDTO> partialUpdate(FavoriteRegionDTO favoriteRegionDTO) {
        LOG.debug("Request to partially update FavoriteRegion : {}", favoriteRegionDTO);

        return favoriteRegionRepository
            .findById(favoriteRegionDTO.getId())
            .map(existingFavoriteRegion -> {
                favoriteRegionMapper.partialUpdate(existingFavoriteRegion, favoriteRegionDTO);

                return existingFavoriteRegion;
            })
            .map(favoriteRegionRepository::save)
            .map(favoriteRegionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteRegionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all FavoriteRegions");
        return favoriteRegionRepository.findAll(pageable).map(favoriteRegionMapper::toDto);
    }

    public Page<FavoriteRegionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return favoriteRegionRepository.findAllWithEagerRelationships(pageable).map(favoriteRegionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FavoriteRegionDTO> findOne(Long id) {
        LOG.debug("Request to get FavoriteRegion : {}", id);
        return favoriteRegionRepository.findOneWithEagerRelationships(id).map(favoriteRegionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete FavoriteRegion : {}", id);
        favoriteRegionRepository.deleteById(id);
    }
}
