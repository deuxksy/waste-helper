package com.waste.helper.web.rest;

import static com.waste.helper.domain.DisposalGuideAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.DisposalGuide;
import com.waste.helper.domain.enumeration.Source;
import com.waste.helper.repository.DisposalGuideRepository;
import com.waste.helper.service.dto.DisposalGuideDTO;
import com.waste.helper.service.mapper.DisposalGuideMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DisposalGuideResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DisposalGuideResourceIT {

    private static final String DEFAULT_WASTE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_WASTE_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DISPOSAL_METHOD = "AAAAAAAAAA";
    private static final String UPDATED_DISPOSAL_METHOD = "BBBBBBBBBB";

    private static final Source DEFAULT_SOURCE = Source.PUBLIC_API;
    private static final Source UPDATED_SOURCE = Source.LLM_GENERATED;

    private static final String ENTITY_API_URL = "/api/disposal-guides";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DisposalGuideRepository disposalGuideRepository;

    @Autowired
    private DisposalGuideMapper disposalGuideMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDisposalGuideMockMvc;

    private DisposalGuide disposalGuide;

    private DisposalGuide insertedDisposalGuide;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DisposalGuide createEntity() {
        return new DisposalGuide().wasteType(DEFAULT_WASTE_TYPE).disposalMethod(DEFAULT_DISPOSAL_METHOD).source(DEFAULT_SOURCE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DisposalGuide createUpdatedEntity() {
        return new DisposalGuide().wasteType(UPDATED_WASTE_TYPE).disposalMethod(UPDATED_DISPOSAL_METHOD).source(UPDATED_SOURCE);
    }

    @BeforeEach
    void initTest() {
        disposalGuide = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDisposalGuide != null) {
            disposalGuideRepository.delete(insertedDisposalGuide);
            insertedDisposalGuide = null;
        }
    }

    @Test
    @Transactional
    void createDisposalGuide() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);
        var returnedDisposalGuideDTO = om.readValue(
            restDisposalGuideMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(disposalGuideDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DisposalGuideDTO.class
        );

        // Validate the DisposalGuide in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDisposalGuide = disposalGuideMapper.toEntity(returnedDisposalGuideDTO);
        assertDisposalGuideUpdatableFieldsEquals(returnedDisposalGuide, getPersistedDisposalGuide(returnedDisposalGuide));

        insertedDisposalGuide = returnedDisposalGuide;
    }

    @Test
    @Transactional
    void createDisposalGuideWithExistingId() throws Exception {
        // Create the DisposalGuide with an existing ID
        disposalGuide.setId(1L);
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDisposalGuideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(disposalGuideDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkWasteTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        disposalGuide.setWasteType(null);

        // Create the DisposalGuide, which fails.
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        restDisposalGuideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(disposalGuideDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDisposalGuides() throws Exception {
        // Initialize the database
        insertedDisposalGuide = disposalGuideRepository.saveAndFlush(disposalGuide);

        // Get all the disposalGuideList
        restDisposalGuideMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(disposalGuide.getId().intValue())))
            .andExpect(jsonPath("$.[*].wasteType").value(hasItem(DEFAULT_WASTE_TYPE)))
            .andExpect(jsonPath("$.[*].disposalMethod").value(hasItem(DEFAULT_DISPOSAL_METHOD)))
            .andExpect(jsonPath("$.[*].source").value(hasItem(DEFAULT_SOURCE.toString())));
    }

    @Test
    @Transactional
    void getDisposalGuide() throws Exception {
        // Initialize the database
        insertedDisposalGuide = disposalGuideRepository.saveAndFlush(disposalGuide);

        // Get the disposalGuide
        restDisposalGuideMockMvc
            .perform(get(ENTITY_API_URL_ID, disposalGuide.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(disposalGuide.getId().intValue()))
            .andExpect(jsonPath("$.wasteType").value(DEFAULT_WASTE_TYPE))
            .andExpect(jsonPath("$.disposalMethod").value(DEFAULT_DISPOSAL_METHOD))
            .andExpect(jsonPath("$.source").value(DEFAULT_SOURCE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingDisposalGuide() throws Exception {
        // Get the disposalGuide
        restDisposalGuideMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDisposalGuide() throws Exception {
        // Initialize the database
        insertedDisposalGuide = disposalGuideRepository.saveAndFlush(disposalGuide);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the disposalGuide
        DisposalGuide updatedDisposalGuide = disposalGuideRepository.findById(disposalGuide.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDisposalGuide are not directly saved in db
        em.detach(updatedDisposalGuide);
        updatedDisposalGuide.wasteType(UPDATED_WASTE_TYPE).disposalMethod(UPDATED_DISPOSAL_METHOD).source(UPDATED_SOURCE);
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(updatedDisposalGuide);

        restDisposalGuideMockMvc
            .perform(
                put(ENTITY_API_URL_ID, disposalGuideDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(disposalGuideDTO))
            )
            .andExpect(status().isOk());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDisposalGuideToMatchAllProperties(updatedDisposalGuide);
    }

    @Test
    @Transactional
    void putNonExistingDisposalGuide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        disposalGuide.setId(longCount.incrementAndGet());

        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDisposalGuideMockMvc
            .perform(
                put(ENTITY_API_URL_ID, disposalGuideDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(disposalGuideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDisposalGuide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        disposalGuide.setId(longCount.incrementAndGet());

        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDisposalGuideMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(disposalGuideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDisposalGuide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        disposalGuide.setId(longCount.incrementAndGet());

        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDisposalGuideMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(disposalGuideDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDisposalGuideWithPatch() throws Exception {
        // Initialize the database
        insertedDisposalGuide = disposalGuideRepository.saveAndFlush(disposalGuide);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the disposalGuide using partial update
        DisposalGuide partialUpdatedDisposalGuide = new DisposalGuide();
        partialUpdatedDisposalGuide.setId(disposalGuide.getId());

        partialUpdatedDisposalGuide.disposalMethod(UPDATED_DISPOSAL_METHOD).source(UPDATED_SOURCE);

        restDisposalGuideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDisposalGuide.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDisposalGuide))
            )
            .andExpect(status().isOk());

        // Validate the DisposalGuide in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDisposalGuideUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDisposalGuide, disposalGuide),
            getPersistedDisposalGuide(disposalGuide)
        );
    }

    @Test
    @Transactional
    void fullUpdateDisposalGuideWithPatch() throws Exception {
        // Initialize the database
        insertedDisposalGuide = disposalGuideRepository.saveAndFlush(disposalGuide);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the disposalGuide using partial update
        DisposalGuide partialUpdatedDisposalGuide = new DisposalGuide();
        partialUpdatedDisposalGuide.setId(disposalGuide.getId());

        partialUpdatedDisposalGuide.wasteType(UPDATED_WASTE_TYPE).disposalMethod(UPDATED_DISPOSAL_METHOD).source(UPDATED_SOURCE);

        restDisposalGuideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDisposalGuide.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDisposalGuide))
            )
            .andExpect(status().isOk());

        // Validate the DisposalGuide in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDisposalGuideUpdatableFieldsEquals(partialUpdatedDisposalGuide, getPersistedDisposalGuide(partialUpdatedDisposalGuide));
    }

    @Test
    @Transactional
    void patchNonExistingDisposalGuide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        disposalGuide.setId(longCount.incrementAndGet());

        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDisposalGuideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, disposalGuideDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(disposalGuideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDisposalGuide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        disposalGuide.setId(longCount.incrementAndGet());

        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDisposalGuideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(disposalGuideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDisposalGuide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        disposalGuide.setId(longCount.incrementAndGet());

        // Create the DisposalGuide
        DisposalGuideDTO disposalGuideDTO = disposalGuideMapper.toDto(disposalGuide);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDisposalGuideMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(disposalGuideDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DisposalGuide in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDisposalGuide() throws Exception {
        // Initialize the database
        insertedDisposalGuide = disposalGuideRepository.saveAndFlush(disposalGuide);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the disposalGuide
        restDisposalGuideMockMvc
            .perform(delete(ENTITY_API_URL_ID, disposalGuide.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return disposalGuideRepository.count();
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

    protected DisposalGuide getPersistedDisposalGuide(DisposalGuide disposalGuide) {
        return disposalGuideRepository.findById(disposalGuide.getId()).orElseThrow();
    }

    protected void assertPersistedDisposalGuideToMatchAllProperties(DisposalGuide expectedDisposalGuide) {
        assertDisposalGuideAllPropertiesEquals(expectedDisposalGuide, getPersistedDisposalGuide(expectedDisposalGuide));
    }

    protected void assertPersistedDisposalGuideToMatchUpdatableProperties(DisposalGuide expectedDisposalGuide) {
        assertDisposalGuideAllUpdatablePropertiesEquals(expectedDisposalGuide, getPersistedDisposalGuide(expectedDisposalGuide));
    }
}
