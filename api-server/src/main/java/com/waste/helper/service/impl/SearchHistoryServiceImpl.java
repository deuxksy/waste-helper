package com.waste.helper.service.impl;

import com.waste.helper.domain.SearchHistory;
import com.waste.helper.repository.SearchHistoryRepository;
import com.waste.helper.service.SearchHistoryService;
import com.waste.helper.service.dto.SearchHistoryDTO;
import com.waste.helper.service.mapper.SearchHistoryMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.waste.helper.domain.SearchHistory}.
 */
@Service
@Transactional
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchHistoryServiceImpl.class);

    private final SearchHistoryRepository searchHistoryRepository;

    private final SearchHistoryMapper searchHistoryMapper;

    public SearchHistoryServiceImpl(SearchHistoryRepository searchHistoryRepository, SearchHistoryMapper searchHistoryMapper) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.searchHistoryMapper = searchHistoryMapper;
    }

    @Override
    public SearchHistoryDTO save(SearchHistoryDTO searchHistoryDTO) {
        LOG.debug("Request to save SearchHistory : {}", searchHistoryDTO);
        SearchHistory searchHistory = searchHistoryMapper.toEntity(searchHistoryDTO);
        searchHistory = searchHistoryRepository.save(searchHistory);
        return searchHistoryMapper.toDto(searchHistory);
    }

    @Override
    public SearchHistoryDTO update(SearchHistoryDTO searchHistoryDTO) {
        LOG.debug("Request to update SearchHistory : {}", searchHistoryDTO);
        SearchHistory searchHistory = searchHistoryMapper.toEntity(searchHistoryDTO);
        searchHistory = searchHistoryRepository.save(searchHistory);
        return searchHistoryMapper.toDto(searchHistory);
    }

    @Override
    public Optional<SearchHistoryDTO> partialUpdate(SearchHistoryDTO searchHistoryDTO) {
        LOG.debug("Request to partially update SearchHistory : {}", searchHistoryDTO);

        return searchHistoryRepository
            .findById(searchHistoryDTO.getId())
            .map(existingSearchHistory -> {
                searchHistoryMapper.partialUpdate(existingSearchHistory, searchHistoryDTO);

                return existingSearchHistory;
            })
            .map(searchHistoryRepository::save)
            .map(searchHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchHistoryDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all SearchHistories");
        return searchHistoryRepository.findAll(pageable).map(searchHistoryMapper::toDto);
    }

    public Page<SearchHistoryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return searchHistoryRepository.findAllWithEagerRelationships(pageable).map(searchHistoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchHistoryDTO> findOne(Long id) {
        LOG.debug("Request to get SearchHistory : {}", id);
        return searchHistoryRepository.findOneWithEagerRelationships(id).map(searchHistoryMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete SearchHistory : {}", id);
        searchHistoryRepository.deleteById(id);
    }
}
