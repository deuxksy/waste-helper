package com.waste.helper.web.rest;

import static com.waste.helper.domain.WasteClassificationAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.WasteClassification;
import com.waste.helper.repository.UserRepository;
import com.waste.helper.repository.WasteClassificationRepository;
import com.waste.helper.service.WasteClassificationService;
import com.waste.helper.service.dto.WasteClassificationDTO;
import com.waste.helper.service.mapper.WasteClassificationMapper;
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
 * Integration tests for the {@link WasteClassificationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class WasteClassificationResourceIT {

    private static final String DEFAULT_DETECTED_CLASS = "AAAAAAAAAA";
    private static final String UPDATED_DETECTED_CLASS = "BBBBBBBBBB";

    private static final Float DEFAULT_CONFIDENCE = 0F;
    private static final Float UPDATED_CONFIDENCE = 1F;

    private static final String DEFAULT_IMAGE_URL = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE_URL = "BBBBBBBBBB";

    private static final String DEFAULT_DETAIL_RESULT = "AAAAAAAAAA";
    private static final String UPDATED_DETAIL_RESULT = "BBBBBBBBBB";

    private static final Instant DEFAULT_CLASSIFIED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CLASSIFIED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/waste-classifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WasteClassificationRepository wasteClassificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private WasteClassificationRepository wasteClassificationRepositoryMock;

    @Autowired
    private WasteClassificationMapper wasteClassificationMapper;

    @Mock
    private WasteClassificationService wasteClassificationServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWasteClassificationMockMvc;

    private WasteClassification wasteClassification;

    private WasteClassification insertedWasteClassification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WasteClassification createEntity() {
        return new WasteClassification()
            .detectedClass(DEFAULT_DETECTED_CLASS)
            .confidence(DEFAULT_CONFIDENCE)
            .imageUrl(DEFAULT_IMAGE_URL)
            .detailResult(DEFAULT_DETAIL_RESULT)
            .classifiedAt(DEFAULT_CLASSIFIED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WasteClassification createUpdatedEntity() {
        return new WasteClassification()
            .detectedClass(UPDATED_DETECTED_CLASS)
            .confidence(UPDATED_CONFIDENCE)
            .imageUrl(UPDATED_IMAGE_URL)
            .detailResult(UPDATED_DETAIL_RESULT)
            .classifiedAt(UPDATED_CLASSIFIED_AT);
    }

    @BeforeEach
    void initTest() {
        wasteClassification = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedWasteClassification != null) {
            wasteClassificationRepository.delete(insertedWasteClassification);
            insertedWasteClassification = null;
        }
    }

    @Test
    @Transactional
    void createWasteClassification() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);
        var returnedWasteClassificationDTO = om.readValue(
            restWasteClassificationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteClassificationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            WasteClassificationDTO.class
        );

        // Validate the WasteClassification in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedWasteClassification = wasteClassificationMapper.toEntity(returnedWasteClassificationDTO);
        assertWasteClassificationUpdatableFieldsEquals(
            returnedWasteClassification,
            getPersistedWasteClassification(returnedWasteClassification)
        );

        insertedWasteClassification = returnedWasteClassification;
    }

    @Test
    @Transactional
    void createWasteClassificationWithExistingId() throws Exception {
        // Create the WasteClassification with an existing ID
        wasteClassification.setId(1L);
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWasteClassificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteClassificationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDetectedClassIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        wasteClassification.setDetectedClass(null);

        // Create the WasteClassification, which fails.
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        restWasteClassificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteClassificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkConfidenceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        wasteClassification.setConfidence(null);

        // Create the WasteClassification, which fails.
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        restWasteClassificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteClassificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkClassifiedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        wasteClassification.setClassifiedAt(null);

        // Create the WasteClassification, which fails.
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        restWasteClassificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteClassificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllWasteClassifications() throws Exception {
        // Initialize the database
        insertedWasteClassification = wasteClassificationRepository.saveAndFlush(wasteClassification);

        // Get all the wasteClassificationList
        restWasteClassificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wasteClassification.getId().intValue())))
            .andExpect(jsonPath("$.[*].detectedClass").value(hasItem(DEFAULT_DETECTED_CLASS)))
            .andExpect(jsonPath("$.[*].confidence").value(hasItem(DEFAULT_CONFIDENCE.doubleValue())))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGE_URL)))
            .andExpect(jsonPath("$.[*].detailResult").value(hasItem(DEFAULT_DETAIL_RESULT)))
            .andExpect(jsonPath("$.[*].classifiedAt").value(hasItem(DEFAULT_CLASSIFIED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllWasteClassificationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(wasteClassificationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restWasteClassificationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(wasteClassificationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllWasteClassificationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(wasteClassificationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restWasteClassificationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(wasteClassificationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getWasteClassification() throws Exception {
        // Initialize the database
        insertedWasteClassification = wasteClassificationRepository.saveAndFlush(wasteClassification);

        // Get the wasteClassification
        restWasteClassificationMockMvc
            .perform(get(ENTITY_API_URL_ID, wasteClassification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(wasteClassification.getId().intValue()))
            .andExpect(jsonPath("$.detectedClass").value(DEFAULT_DETECTED_CLASS))
            .andExpect(jsonPath("$.confidence").value(DEFAULT_CONFIDENCE.doubleValue()))
            .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGE_URL))
            .andExpect(jsonPath("$.detailResult").value(DEFAULT_DETAIL_RESULT))
            .andExpect(jsonPath("$.classifiedAt").value(DEFAULT_CLASSIFIED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingWasteClassification() throws Exception {
        // Get the wasteClassification
        restWasteClassificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWasteClassification() throws Exception {
        // Initialize the database
        insertedWasteClassification = wasteClassificationRepository.saveAndFlush(wasteClassification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wasteClassification
        WasteClassification updatedWasteClassification = wasteClassificationRepository.findById(wasteClassification.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWasteClassification are not directly saved in db
        em.detach(updatedWasteClassification);
        updatedWasteClassification
            .detectedClass(UPDATED_DETECTED_CLASS)
            .confidence(UPDATED_CONFIDENCE)
            .imageUrl(UPDATED_IMAGE_URL)
            .detailResult(UPDATED_DETAIL_RESULT)
            .classifiedAt(UPDATED_CLASSIFIED_AT);
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(updatedWasteClassification);

        restWasteClassificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wasteClassificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wasteClassificationDTO))
            )
            .andExpect(status().isOk());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWasteClassificationToMatchAllProperties(updatedWasteClassification);
    }

    @Test
    @Transactional
    void putNonExistingWasteClassification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteClassification.setId(longCount.incrementAndGet());

        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWasteClassificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wasteClassificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wasteClassificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWasteClassification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteClassification.setId(longCount.incrementAndGet());

        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteClassificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wasteClassificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWasteClassification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteClassification.setId(longCount.incrementAndGet());

        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteClassificationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteClassificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWasteClassificationWithPatch() throws Exception {
        // Initialize the database
        insertedWasteClassification = wasteClassificationRepository.saveAndFlush(wasteClassification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wasteClassification using partial update
        WasteClassification partialUpdatedWasteClassification = new WasteClassification();
        partialUpdatedWasteClassification.setId(wasteClassification.getId());

        partialUpdatedWasteClassification
            .detectedClass(UPDATED_DETECTED_CLASS)
            .detailResult(UPDATED_DETAIL_RESULT)
            .classifiedAt(UPDATED_CLASSIFIED_AT);

        restWasteClassificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWasteClassification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWasteClassification))
            )
            .andExpect(status().isOk());

        // Validate the WasteClassification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWasteClassificationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedWasteClassification, wasteClassification),
            getPersistedWasteClassification(wasteClassification)
        );
    }

    @Test
    @Transactional
    void fullUpdateWasteClassificationWithPatch() throws Exception {
        // Initialize the database
        insertedWasteClassification = wasteClassificationRepository.saveAndFlush(wasteClassification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wasteClassification using partial update
        WasteClassification partialUpdatedWasteClassification = new WasteClassification();
        partialUpdatedWasteClassification.setId(wasteClassification.getId());

        partialUpdatedWasteClassification
            .detectedClass(UPDATED_DETECTED_CLASS)
            .confidence(UPDATED_CONFIDENCE)
            .imageUrl(UPDATED_IMAGE_URL)
            .detailResult(UPDATED_DETAIL_RESULT)
            .classifiedAt(UPDATED_CLASSIFIED_AT);

        restWasteClassificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWasteClassification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWasteClassification))
            )
            .andExpect(status().isOk());

        // Validate the WasteClassification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWasteClassificationUpdatableFieldsEquals(
            partialUpdatedWasteClassification,
            getPersistedWasteClassification(partialUpdatedWasteClassification)
        );
    }

    @Test
    @Transactional
    void patchNonExistingWasteClassification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteClassification.setId(longCount.incrementAndGet());

        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWasteClassificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, wasteClassificationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wasteClassificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWasteClassification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteClassification.setId(longCount.incrementAndGet());

        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteClassificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wasteClassificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWasteClassification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteClassification.setId(longCount.incrementAndGet());

        // Create the WasteClassification
        WasteClassificationDTO wasteClassificationDTO = wasteClassificationMapper.toDto(wasteClassification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteClassificationMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(wasteClassificationDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the WasteClassification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWasteClassification() throws Exception {
        // Initialize the database
        insertedWasteClassification = wasteClassificationRepository.saveAndFlush(wasteClassification);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the wasteClassification
        restWasteClassificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, wasteClassification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return wasteClassificationRepository.count();
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

    protected WasteClassification getPersistedWasteClassification(WasteClassification wasteClassification) {
        return wasteClassificationRepository.findById(wasteClassification.getId()).orElseThrow();
    }

    protected void assertPersistedWasteClassificationToMatchAllProperties(WasteClassification expectedWasteClassification) {
        assertWasteClassificationAllPropertiesEquals(
            expectedWasteClassification,
            getPersistedWasteClassification(expectedWasteClassification)
        );
    }

    protected void assertPersistedWasteClassificationToMatchUpdatableProperties(WasteClassification expectedWasteClassification) {
        assertWasteClassificationAllUpdatablePropertiesEquals(
            expectedWasteClassification,
            getPersistedWasteClassification(expectedWasteClassification)
        );
    }
}
