package com.waste.helper.web.rest;

import static com.waste.helper.domain.WasteImageAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.WasteImage;
import com.waste.helper.repository.WasteImageRepository;
import com.waste.helper.service.dto.WasteImageDTO;
import com.waste.helper.service.mapper.WasteImageMapper;
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
 * Integration tests for the {@link WasteImageResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WasteImageResourceIT {

    private static final String DEFAULT_ORIGINAL_URL = "AAAAAAAAAA";
    private static final String UPDATED_ORIGINAL_URL = "BBBBBBBBBB";

    private static final String DEFAULT_THUMBNAIL_URL = "AAAAAAAAAA";
    private static final String UPDATED_THUMBNAIL_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/waste-images";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WasteImageRepository wasteImageRepository;

    @Autowired
    private WasteImageMapper wasteImageMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWasteImageMockMvc;

    private WasteImage wasteImage;

    private WasteImage insertedWasteImage;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WasteImage createEntity() {
        return new WasteImage().originalUrl(DEFAULT_ORIGINAL_URL).thumbnailUrl(DEFAULT_THUMBNAIL_URL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WasteImage createUpdatedEntity() {
        return new WasteImage().originalUrl(UPDATED_ORIGINAL_URL).thumbnailUrl(UPDATED_THUMBNAIL_URL);
    }

    @BeforeEach
    void initTest() {
        wasteImage = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedWasteImage != null) {
            wasteImageRepository.delete(insertedWasteImage);
            insertedWasteImage = null;
        }
    }

    @Test
    @Transactional
    void createWasteImage() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);
        var returnedWasteImageDTO = om.readValue(
            restWasteImageMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteImageDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            WasteImageDTO.class
        );

        // Validate the WasteImage in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedWasteImage = wasteImageMapper.toEntity(returnedWasteImageDTO);
        assertWasteImageUpdatableFieldsEquals(returnedWasteImage, getPersistedWasteImage(returnedWasteImage));

        insertedWasteImage = returnedWasteImage;
    }

    @Test
    @Transactional
    void createWasteImageWithExistingId() throws Exception {
        // Create the WasteImage with an existing ID
        wasteImage.setId(1L);
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWasteImageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteImageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllWasteImages() throws Exception {
        // Initialize the database
        insertedWasteImage = wasteImageRepository.saveAndFlush(wasteImage);

        // Get all the wasteImageList
        restWasteImageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wasteImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].originalUrl").value(hasItem(DEFAULT_ORIGINAL_URL)))
            .andExpect(jsonPath("$.[*].thumbnailUrl").value(hasItem(DEFAULT_THUMBNAIL_URL)));
    }

    @Test
    @Transactional
    void getWasteImage() throws Exception {
        // Initialize the database
        insertedWasteImage = wasteImageRepository.saveAndFlush(wasteImage);

        // Get the wasteImage
        restWasteImageMockMvc
            .perform(get(ENTITY_API_URL_ID, wasteImage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(wasteImage.getId().intValue()))
            .andExpect(jsonPath("$.originalUrl").value(DEFAULT_ORIGINAL_URL))
            .andExpect(jsonPath("$.thumbnailUrl").value(DEFAULT_THUMBNAIL_URL));
    }

    @Test
    @Transactional
    void getNonExistingWasteImage() throws Exception {
        // Get the wasteImage
        restWasteImageMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWasteImage() throws Exception {
        // Initialize the database
        insertedWasteImage = wasteImageRepository.saveAndFlush(wasteImage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wasteImage
        WasteImage updatedWasteImage = wasteImageRepository.findById(wasteImage.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWasteImage are not directly saved in db
        em.detach(updatedWasteImage);
        updatedWasteImage.originalUrl(UPDATED_ORIGINAL_URL).thumbnailUrl(UPDATED_THUMBNAIL_URL);
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(updatedWasteImage);

        restWasteImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wasteImageDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wasteImageDTO))
            )
            .andExpect(status().isOk());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWasteImageToMatchAllProperties(updatedWasteImage);
    }

    @Test
    @Transactional
    void putNonExistingWasteImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteImage.setId(longCount.incrementAndGet());

        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWasteImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wasteImageDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wasteImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWasteImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteImage.setId(longCount.incrementAndGet());

        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wasteImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWasteImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteImage.setId(longCount.incrementAndGet());

        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteImageMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wasteImageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWasteImageWithPatch() throws Exception {
        // Initialize the database
        insertedWasteImage = wasteImageRepository.saveAndFlush(wasteImage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wasteImage using partial update
        WasteImage partialUpdatedWasteImage = new WasteImage();
        partialUpdatedWasteImage.setId(wasteImage.getId());

        partialUpdatedWasteImage.thumbnailUrl(UPDATED_THUMBNAIL_URL);

        restWasteImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWasteImage.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWasteImage))
            )
            .andExpect(status().isOk());

        // Validate the WasteImage in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWasteImageUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedWasteImage, wasteImage),
            getPersistedWasteImage(wasteImage)
        );
    }

    @Test
    @Transactional
    void fullUpdateWasteImageWithPatch() throws Exception {
        // Initialize the database
        insertedWasteImage = wasteImageRepository.saveAndFlush(wasteImage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wasteImage using partial update
        WasteImage partialUpdatedWasteImage = new WasteImage();
        partialUpdatedWasteImage.setId(wasteImage.getId());

        partialUpdatedWasteImage.originalUrl(UPDATED_ORIGINAL_URL).thumbnailUrl(UPDATED_THUMBNAIL_URL);

        restWasteImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWasteImage.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWasteImage))
            )
            .andExpect(status().isOk());

        // Validate the WasteImage in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWasteImageUpdatableFieldsEquals(partialUpdatedWasteImage, getPersistedWasteImage(partialUpdatedWasteImage));
    }

    @Test
    @Transactional
    void patchNonExistingWasteImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteImage.setId(longCount.incrementAndGet());

        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWasteImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, wasteImageDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wasteImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWasteImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteImage.setId(longCount.incrementAndGet());

        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wasteImageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWasteImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wasteImage.setId(longCount.incrementAndGet());

        // Create the WasteImage
        WasteImageDTO wasteImageDTO = wasteImageMapper.toDto(wasteImage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWasteImageMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(wasteImageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WasteImage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWasteImage() throws Exception {
        // Initialize the database
        insertedWasteImage = wasteImageRepository.saveAndFlush(wasteImage);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the wasteImage
        restWasteImageMockMvc
            .perform(delete(ENTITY_API_URL_ID, wasteImage.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return wasteImageRepository.count();
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

    protected WasteImage getPersistedWasteImage(WasteImage wasteImage) {
        return wasteImageRepository.findById(wasteImage.getId()).orElseThrow();
    }

    protected void assertPersistedWasteImageToMatchAllProperties(WasteImage expectedWasteImage) {
        assertWasteImageAllPropertiesEquals(expectedWasteImage, getPersistedWasteImage(expectedWasteImage));
    }

    protected void assertPersistedWasteImageToMatchUpdatableProperties(WasteImage expectedWasteImage) {
        assertWasteImageAllUpdatablePropertiesEquals(expectedWasteImage, getPersistedWasteImage(expectedWasteImage));
    }
}
