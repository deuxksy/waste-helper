package com.waste.helper.web.rest;

import static com.waste.helper.domain.FeedbackAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.Feedback;
import com.waste.helper.repository.FeedbackRepository;
import com.waste.helper.repository.UserRepository;
import com.waste.helper.service.FeedbackService;
import com.waste.helper.service.dto.FeedbackDTO;
import com.waste.helper.service.mapper.FeedbackMapper;
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
 * Integration tests for the {@link FeedbackResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FeedbackResourceIT {

    private static final Boolean DEFAULT_IS_ACCURATE = false;
    private static final Boolean UPDATED_IS_ACCURATE = true;

    private static final String DEFAULT_CORRECTED_CLASS = "AAAAAAAAAA";
    private static final String UPDATED_CORRECTED_CLASS = "BBBBBBBBBB";

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/feedbacks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private FeedbackRepository feedbackRepositoryMock;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Mock
    private FeedbackService feedbackServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFeedbackMockMvc;

    private Feedback feedback;

    private Feedback insertedFeedback;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Feedback createEntity() {
        return new Feedback().isAccurate(DEFAULT_IS_ACCURATE).correctedClass(DEFAULT_CORRECTED_CLASS).comment(DEFAULT_COMMENT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Feedback createUpdatedEntity() {
        return new Feedback().isAccurate(UPDATED_IS_ACCURATE).correctedClass(UPDATED_CORRECTED_CLASS).comment(UPDATED_COMMENT);
    }

    @BeforeEach
    void initTest() {
        feedback = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedFeedback != null) {
            feedbackRepository.delete(insertedFeedback);
            insertedFeedback = null;
        }
    }

    @Test
    @Transactional
    void createFeedback() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);
        var returnedFeedbackDTO = om.readValue(
            restFeedbackMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedbackDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FeedbackDTO.class
        );

        // Validate the Feedback in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFeedback = feedbackMapper.toEntity(returnedFeedbackDTO);
        assertFeedbackUpdatableFieldsEquals(returnedFeedback, getPersistedFeedback(returnedFeedback));

        insertedFeedback = returnedFeedback;
    }

    @Test
    @Transactional
    void createFeedbackWithExistingId() throws Exception {
        // Create the Feedback with an existing ID
        feedback.setId(1L);
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFeedbackMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedbackDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllFeedbacks() throws Exception {
        // Initialize the database
        insertedFeedback = feedbackRepository.saveAndFlush(feedback);

        // Get all the feedbackList
        restFeedbackMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(feedback.getId().intValue())))
            .andExpect(jsonPath("$.[*].isAccurate").value(hasItem(DEFAULT_IS_ACCURATE)))
            .andExpect(jsonPath("$.[*].correctedClass").value(hasItem(DEFAULT_CORRECTED_CLASS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFeedbacksWithEagerRelationshipsIsEnabled() throws Exception {
        when(feedbackServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFeedbackMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(feedbackServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFeedbacksWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(feedbackServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFeedbackMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(feedbackRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFeedback() throws Exception {
        // Initialize the database
        insertedFeedback = feedbackRepository.saveAndFlush(feedback);

        // Get the feedback
        restFeedbackMockMvc
            .perform(get(ENTITY_API_URL_ID, feedback.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(feedback.getId().intValue()))
            .andExpect(jsonPath("$.isAccurate").value(DEFAULT_IS_ACCURATE))
            .andExpect(jsonPath("$.correctedClass").value(DEFAULT_CORRECTED_CLASS))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT));
    }

    @Test
    @Transactional
    void getNonExistingFeedback() throws Exception {
        // Get the feedback
        restFeedbackMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFeedback() throws Exception {
        // Initialize the database
        insertedFeedback = feedbackRepository.saveAndFlush(feedback);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the feedback
        Feedback updatedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFeedback are not directly saved in db
        em.detach(updatedFeedback);
        updatedFeedback.isAccurate(UPDATED_IS_ACCURATE).correctedClass(UPDATED_CORRECTED_CLASS).comment(UPDATED_COMMENT);
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(updatedFeedback);

        restFeedbackMockMvc
            .perform(
                put(ENTITY_API_URL_ID, feedbackDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(feedbackDTO))
            )
            .andExpect(status().isOk());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFeedbackToMatchAllProperties(updatedFeedback);
    }

    @Test
    @Transactional
    void putNonExistingFeedback() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedback.setId(longCount.incrementAndGet());

        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFeedbackMockMvc
            .perform(
                put(ENTITY_API_URL_ID, feedbackDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(feedbackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFeedback() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedback.setId(longCount.incrementAndGet());

        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedbackMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(feedbackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFeedback() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedback.setId(longCount.incrementAndGet());

        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedbackMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedbackDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFeedbackWithPatch() throws Exception {
        // Initialize the database
        insertedFeedback = feedbackRepository.saveAndFlush(feedback);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the feedback using partial update
        Feedback partialUpdatedFeedback = new Feedback();
        partialUpdatedFeedback.setId(feedback.getId());

        partialUpdatedFeedback.isAccurate(UPDATED_IS_ACCURATE).correctedClass(UPDATED_CORRECTED_CLASS).comment(UPDATED_COMMENT);

        restFeedbackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFeedback.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFeedback))
            )
            .andExpect(status().isOk());

        // Validate the Feedback in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFeedbackUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFeedback, feedback), getPersistedFeedback(feedback));
    }

    @Test
    @Transactional
    void fullUpdateFeedbackWithPatch() throws Exception {
        // Initialize the database
        insertedFeedback = feedbackRepository.saveAndFlush(feedback);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the feedback using partial update
        Feedback partialUpdatedFeedback = new Feedback();
        partialUpdatedFeedback.setId(feedback.getId());

        partialUpdatedFeedback.isAccurate(UPDATED_IS_ACCURATE).correctedClass(UPDATED_CORRECTED_CLASS).comment(UPDATED_COMMENT);

        restFeedbackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFeedback.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFeedback))
            )
            .andExpect(status().isOk());

        // Validate the Feedback in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFeedbackUpdatableFieldsEquals(partialUpdatedFeedback, getPersistedFeedback(partialUpdatedFeedback));
    }

    @Test
    @Transactional
    void patchNonExistingFeedback() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedback.setId(longCount.incrementAndGet());

        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFeedbackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, feedbackDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(feedbackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFeedback() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedback.setId(longCount.incrementAndGet());

        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedbackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(feedbackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFeedback() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedback.setId(longCount.incrementAndGet());

        // Create the Feedback
        FeedbackDTO feedbackDTO = feedbackMapper.toDto(feedback);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedbackMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(feedbackDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Feedback in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFeedback() throws Exception {
        // Initialize the database
        insertedFeedback = feedbackRepository.saveAndFlush(feedback);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the feedback
        restFeedbackMockMvc
            .perform(delete(ENTITY_API_URL_ID, feedback.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return feedbackRepository.count();
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

    protected Feedback getPersistedFeedback(Feedback feedback) {
        return feedbackRepository.findById(feedback.getId()).orElseThrow();
    }

    protected void assertPersistedFeedbackToMatchAllProperties(Feedback expectedFeedback) {
        assertFeedbackAllPropertiesEquals(expectedFeedback, getPersistedFeedback(expectedFeedback));
    }

    protected void assertPersistedFeedbackToMatchUpdatableProperties(Feedback expectedFeedback) {
        assertFeedbackAllUpdatablePropertiesEquals(expectedFeedback, getPersistedFeedback(expectedFeedback));
    }
}
