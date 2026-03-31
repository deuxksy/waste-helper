package com.waste.helper.web.rest;

import static com.waste.helper.domain.FavoriteRegionAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.FavoriteRegion;
import com.waste.helper.repository.FavoriteRegionRepository;
import com.waste.helper.repository.UserRepository;
import com.waste.helper.service.FavoriteRegionService;
import com.waste.helper.service.dto.FavoriteRegionDTO;
import com.waste.helper.service.mapper.FavoriteRegionMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link FavoriteRegionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FavoriteRegionResourceIT {

    private static final String ENTITY_API_URL = "/api/favorite-regions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FavoriteRegionRepository favoriteRegionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private FavoriteRegionRepository favoriteRegionRepositoryMock;

    @Autowired
    private FavoriteRegionMapper favoriteRegionMapper;

    @Mock
    private FavoriteRegionService favoriteRegionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFavoriteRegionMockMvc;

    private FavoriteRegion favoriteRegion;

    private FavoriteRegion insertedFavoriteRegion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FavoriteRegion createEntity() {
        return new FavoriteRegion();
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FavoriteRegion createUpdatedEntity() {
        return new FavoriteRegion();
    }

    @BeforeEach
    void initTest() {
        favoriteRegion = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedFavoriteRegion != null) {
            favoriteRegionRepository.delete(insertedFavoriteRegion);
            insertedFavoriteRegion = null;
        }
    }

    @Test
    @Transactional
    void createFavoriteRegion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);
        var returnedFavoriteRegionDTO = om.readValue(
            restFavoriteRegionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteRegionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FavoriteRegionDTO.class
        );

        // Validate the FavoriteRegion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFavoriteRegion = favoriteRegionMapper.toEntity(returnedFavoriteRegionDTO);
        assertFavoriteRegionUpdatableFieldsEquals(returnedFavoriteRegion, getPersistedFavoriteRegion(returnedFavoriteRegion));

        insertedFavoriteRegion = returnedFavoriteRegion;
    }

    @Test
    @Transactional
    void createFavoriteRegionWithExistingId() throws Exception {
        // Create the FavoriteRegion with an existing ID
        favoriteRegion.setId(1L);
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFavoriteRegionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteRegionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllFavoriteRegions() throws Exception {
        // Initialize the database
        insertedFavoriteRegion = favoriteRegionRepository.saveAndFlush(favoriteRegion);

        // Get all the favoriteRegionList
        restFavoriteRegionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favoriteRegion.getId().intValue())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFavoriteRegionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(favoriteRegionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFavoriteRegionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(favoriteRegionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFavoriteRegionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(favoriteRegionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFavoriteRegionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(favoriteRegionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFavoriteRegion() throws Exception {
        // Initialize the database
        insertedFavoriteRegion = favoriteRegionRepository.saveAndFlush(favoriteRegion);

        // Get the favoriteRegion
        restFavoriteRegionMockMvc
            .perform(get(ENTITY_API_URL_ID, favoriteRegion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(favoriteRegion.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingFavoriteRegion() throws Exception {
        // Get the favoriteRegion
        restFavoriteRegionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFavoriteRegion() throws Exception {
        // Initialize the database
        insertedFavoriteRegion = favoriteRegionRepository.saveAndFlush(favoriteRegion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favoriteRegion
        FavoriteRegion updatedFavoriteRegion = favoriteRegionRepository.findById(favoriteRegion.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFavoriteRegion are not directly saved in db
        em.detach(updatedFavoriteRegion);
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(updatedFavoriteRegion);

        restFavoriteRegionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, favoriteRegionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriteRegionDTO))
            )
            .andExpect(status().isOk());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFavoriteRegionToMatchAllProperties(updatedFavoriteRegion);
    }

    @Test
    @Transactional
    void putNonExistingFavoriteRegion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteRegion.setId(longCount.incrementAndGet());

        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFavoriteRegionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, favoriteRegionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriteRegionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFavoriteRegion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteRegion.setId(longCount.incrementAndGet());

        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteRegionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriteRegionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFavoriteRegion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteRegion.setId(longCount.incrementAndGet());

        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteRegionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteRegionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFavoriteRegionWithPatch() throws Exception {
        // Initialize the database
        insertedFavoriteRegion = favoriteRegionRepository.saveAndFlush(favoriteRegion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favoriteRegion using partial update
        FavoriteRegion partialUpdatedFavoriteRegion = new FavoriteRegion();
        partialUpdatedFavoriteRegion.setId(favoriteRegion.getId());

        restFavoriteRegionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFavoriteRegion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFavoriteRegion))
            )
            .andExpect(status().isOk());

        // Validate the FavoriteRegion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFavoriteRegionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedFavoriteRegion, favoriteRegion),
            getPersistedFavoriteRegion(favoriteRegion)
        );
    }

    @Test
    @Transactional
    void fullUpdateFavoriteRegionWithPatch() throws Exception {
        // Initialize the database
        insertedFavoriteRegion = favoriteRegionRepository.saveAndFlush(favoriteRegion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favoriteRegion using partial update
        FavoriteRegion partialUpdatedFavoriteRegion = new FavoriteRegion();
        partialUpdatedFavoriteRegion.setId(favoriteRegion.getId());

        restFavoriteRegionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFavoriteRegion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFavoriteRegion))
            )
            .andExpect(status().isOk());

        // Validate the FavoriteRegion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFavoriteRegionUpdatableFieldsEquals(partialUpdatedFavoriteRegion, getPersistedFavoriteRegion(partialUpdatedFavoriteRegion));
    }

    @Test
    @Transactional
    void patchNonExistingFavoriteRegion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteRegion.setId(longCount.incrementAndGet());

        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFavoriteRegionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, favoriteRegionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(favoriteRegionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFavoriteRegion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteRegion.setId(longCount.incrementAndGet());

        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteRegionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(favoriteRegionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFavoriteRegion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteRegion.setId(longCount.incrementAndGet());

        // Create the FavoriteRegion
        FavoriteRegionDTO favoriteRegionDTO = favoriteRegionMapper.toDto(favoriteRegion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteRegionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(favoriteRegionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FavoriteRegion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFavoriteRegion() throws Exception {
        // Initialize the database
        insertedFavoriteRegion = favoriteRegionRepository.saveAndFlush(favoriteRegion);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the favoriteRegion
        restFavoriteRegionMockMvc
            .perform(delete(ENTITY_API_URL_ID, favoriteRegion.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return favoriteRegionRepository.count();
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

    protected FavoriteRegion getPersistedFavoriteRegion(FavoriteRegion favoriteRegion) {
        return favoriteRegionRepository.findById(favoriteRegion.getId()).orElseThrow();
    }

    protected void assertPersistedFavoriteRegionToMatchAllProperties(FavoriteRegion expectedFavoriteRegion) {
        assertFavoriteRegionAllPropertiesEquals(expectedFavoriteRegion, getPersistedFavoriteRegion(expectedFavoriteRegion));
    }

    protected void assertPersistedFavoriteRegionToMatchUpdatableProperties(FavoriteRegion expectedFavoriteRegion) {
        assertFavoriteRegionAllUpdatablePropertiesEquals(expectedFavoriteRegion, getPersistedFavoriteRegion(expectedFavoriteRegion));
    }
}
