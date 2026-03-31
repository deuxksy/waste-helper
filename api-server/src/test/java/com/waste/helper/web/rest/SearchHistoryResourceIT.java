package com.waste.helper.web.rest;

import static com.waste.helper.domain.SearchHistoryAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.SearchHistory;
import com.waste.helper.repository.SearchHistoryRepository;
import com.waste.helper.repository.UserRepository;
import com.waste.helper.service.SearchHistoryService;
import com.waste.helper.service.dto.SearchHistoryDTO;
import com.waste.helper.service.mapper.SearchHistoryMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SearchHistoryResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SearchHistoryResourceIT {

    private static final String DEFAULT_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_QUERY = "BBBBBBBBBB";

    private static final String DEFAULT_RESULT_SUMMARY = "AAAAAAAAAA";
    private static final String UPDATED_RESULT_SUMMARY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CLASSIFIED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CLASSIFIED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/search-histories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepositoryMock;

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Mock
    private SearchHistoryService searchHistoryServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSearchHistoryMockMvc;

    private SearchHistory searchHistory;

    private SearchHistory insertedSearchHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SearchHistory createEntity() {
        return new SearchHistory().query(DEFAULT_QUERY).resultSummary(DEFAULT_RESULT_SUMMARY).classifiedAt(DEFAULT_CLASSIFIED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SearchHistory createUpdatedEntity() {
        return new SearchHistory().query(UPDATED_QUERY).resultSummary(UPDATED_RESULT_SUMMARY).classifiedAt(UPDATED_CLASSIFIED_AT);
    }

    @BeforeEach
    void initTest() {
        searchHistory = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSearchHistory != null) {
            searchHistoryRepository.delete(insertedSearchHistory);
            insertedSearchHistory = null;
        }
    }

    @Test
    @Transactional
    void createSearchHistory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);
        var returnedSearchHistoryDTO = om.readValue(
            restSearchHistoryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistoryDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SearchHistoryDTO.class
        );

        // Validate the SearchHistory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSearchHistory = searchHistoryMapper.toEntity(returnedSearchHistoryDTO);
        assertSearchHistoryUpdatableFieldsEquals(returnedSearchHistory, getPersistedSearchHistory(returnedSearchHistory));

        insertedSearchHistory = returnedSearchHistory;
    }

    @Test
    @Transactional
    void createSearchHistoryWithExistingId() throws Exception {
        // Create the SearchHistory with an existing ID
        searchHistory.setId(1L);
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSearchHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkClassifiedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        searchHistory.setClassifiedAt(null);

        // Create the SearchHistory, which fails.
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        restSearchHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistoryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSearchHistories() throws Exception {
        // Initialize the database
        insertedSearchHistory = searchHistoryRepository.saveAndFlush(searchHistory);

        // Get all the searchHistoryList
        restSearchHistoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(searchHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].query").value(hasItem(DEFAULT_QUERY)))
            .andExpect(jsonPath("$.[*].resultSummary").value(hasItem(DEFAULT_RESULT_SUMMARY)))
            .andExpect(jsonPath("$.[*].classifiedAt").value(hasItem(DEFAULT_CLASSIFIED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSearchHistoriesWithEagerRelationshipsIsEnabled() throws Exception {
        when(searchHistoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSearchHistoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(searchHistoryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSearchHistoriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(searchHistoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSearchHistoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(searchHistoryRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSearchHistory() throws Exception {
        // Initialize the database
        insertedSearchHistory = searchHistoryRepository.saveAndFlush(searchHistory);

        // Get the searchHistory
        restSearchHistoryMockMvc
            .perform(get(ENTITY_API_URL_ID, searchHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(searchHistory.getId().intValue()))
            .andExpect(jsonPath("$.query").value(DEFAULT_QUERY))
            .andExpect(jsonPath("$.resultSummary").value(DEFAULT_RESULT_SUMMARY))
            .andExpect(jsonPath("$.classifiedAt").value(DEFAULT_CLASSIFIED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingSearchHistory() throws Exception {
        // Get the searchHistory
        restSearchHistoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSearchHistory() throws Exception {
        // Initialize the database
        insertedSearchHistory = searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the searchHistory
        SearchHistory updatedSearchHistory = searchHistoryRepository.findById(searchHistory.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSearchHistory are not directly saved in db
        em.detach(updatedSearchHistory);
        updatedSearchHistory.query(UPDATED_QUERY).resultSummary(UPDATED_RESULT_SUMMARY).classifiedAt(UPDATED_CLASSIFIED_AT);
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(updatedSearchHistory);

        restSearchHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, searchHistoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(searchHistoryDTO))
            )
            .andExpect(status().isOk());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSearchHistoryToMatchAllProperties(updatedSearchHistory);
    }

    @Test
    @Transactional
    void putNonExistingSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, searchHistoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(searchHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(searchHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistoryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSearchHistoryWithPatch() throws Exception {
        // Initialize the database
        insertedSearchHistory = searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the searchHistory using partial update
        SearchHistory partialUpdatedSearchHistory = new SearchHistory();
        partialUpdatedSearchHistory.setId(searchHistory.getId());

        partialUpdatedSearchHistory.query(UPDATED_QUERY).resultSummary(UPDATED_RESULT_SUMMARY).classifiedAt(UPDATED_CLASSIFIED_AT);

        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSearchHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSearchHistory))
            )
            .andExpect(status().isOk());

        // Validate the SearchHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSearchHistoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSearchHistory, searchHistory),
            getPersistedSearchHistory(searchHistory)
        );
    }

    @Test
    @Transactional
    void fullUpdateSearchHistoryWithPatch() throws Exception {
        // Initialize the database
        insertedSearchHistory = searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the searchHistory using partial update
        SearchHistory partialUpdatedSearchHistory = new SearchHistory();
        partialUpdatedSearchHistory.setId(searchHistory.getId());

        partialUpdatedSearchHistory.query(UPDATED_QUERY).resultSummary(UPDATED_RESULT_SUMMARY).classifiedAt(UPDATED_CLASSIFIED_AT);

        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSearchHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSearchHistory))
            )
            .andExpect(status().isOk());

        // Validate the SearchHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSearchHistoryUpdatableFieldsEquals(partialUpdatedSearchHistory, getPersistedSearchHistory(partialUpdatedSearchHistory));
    }

    @Test
    @Transactional
    void patchNonExistingSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, searchHistoryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(searchHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(searchHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // Create the SearchHistory
        SearchHistoryDTO searchHistoryDTO = searchHistoryMapper.toDto(searchHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(searchHistoryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSearchHistory() throws Exception {
        // Initialize the database
        insertedSearchHistory = searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the searchHistory
        restSearchHistoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, searchHistory.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return searchHistoryRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected SearchHistory getPersistedSearchHistory(SearchHistory searchHistory) {
        return searchHistoryRepository.findById(searchHistory.getId()).orElseThrow();
    }

    protected void assertPersistedSearchHistoryToMatchAllProperties(SearchHistory expectedSearchHistory) {
        assertSearchHistoryAllPropertiesEquals(expectedSearchHistory, getPersistedSearchHistory(expectedSearchHistory));
    }

    protected void assertPersistedSearchHistoryToMatchUpdatableProperties(SearchHistory expectedSearchHistory) {
        assertSearchHistoryAllUpdatablePropertiesEquals(expectedSearchHistory, getPersistedSearchHistory(expectedSearchHistory));
    }
}
