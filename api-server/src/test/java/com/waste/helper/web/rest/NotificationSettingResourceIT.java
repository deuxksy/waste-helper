package com.waste.helper.web.rest;

import static com.waste.helper.domain.NotificationSettingAsserts.*;
import static com.waste.helper.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waste.helper.IntegrationTest;
import com.waste.helper.domain.NotificationSetting;
import com.waste.helper.repository.NotificationSettingRepository;
import com.waste.helper.repository.UserRepository;
import com.waste.helper.service.NotificationSettingService;
import com.waste.helper.service.dto.NotificationSettingDTO;
import com.waste.helper.service.mapper.NotificationSettingMapper;
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
 * Integration tests for the {@link NotificationSettingResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class NotificationSettingResourceIT {

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    private static final String DEFAULT_FCM_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_FCM_TOKEN = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/notification-settings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepositoryMock;

    @Autowired
    private NotificationSettingMapper notificationSettingMapper;

    @Mock
    private NotificationSettingService notificationSettingServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNotificationSettingMockMvc;

    private NotificationSetting notificationSetting;

    private NotificationSetting insertedNotificationSetting;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NotificationSetting createEntity() {
        return new NotificationSetting().enabled(DEFAULT_ENABLED).fcmToken(DEFAULT_FCM_TOKEN);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NotificationSetting createUpdatedEntity() {
        return new NotificationSetting().enabled(UPDATED_ENABLED).fcmToken(UPDATED_FCM_TOKEN);
    }

    @BeforeEach
    void initTest() {
        notificationSetting = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedNotificationSetting != null) {
            notificationSettingRepository.delete(insertedNotificationSetting);
            insertedNotificationSetting = null;
        }
    }

    @Test
    @Transactional
    void createNotificationSetting() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);
        var returnedNotificationSettingDTO = om.readValue(
            restNotificationSettingMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationSettingDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NotificationSettingDTO.class
        );

        // Validate the NotificationSetting in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedNotificationSetting = notificationSettingMapper.toEntity(returnedNotificationSettingDTO);
        assertNotificationSettingUpdatableFieldsEquals(
            returnedNotificationSetting,
            getPersistedNotificationSetting(returnedNotificationSetting)
        );

        insertedNotificationSetting = returnedNotificationSetting;
    }

    @Test
    @Transactional
    void createNotificationSettingWithExistingId() throws Exception {
        // Create the NotificationSetting with an existing ID
        notificationSetting.setId(1L);
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restNotificationSettingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationSettingDTO)))
            .andExpect(status().isBadRequest());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllNotificationSettings() throws Exception {
        // Initialize the database
        insertedNotificationSetting = notificationSettingRepository.saveAndFlush(notificationSetting);

        // Get all the notificationSettingList
        restNotificationSettingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notificationSetting.getId().intValue())))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED)))
            .andExpect(jsonPath("$.[*].fcmToken").value(hasItem(DEFAULT_FCM_TOKEN)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNotificationSettingsWithEagerRelationshipsIsEnabled() throws Exception {
        when(notificationSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNotificationSettingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(notificationSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNotificationSettingsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(notificationSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNotificationSettingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(notificationSettingRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getNotificationSetting() throws Exception {
        // Initialize the database
        insertedNotificationSetting = notificationSettingRepository.saveAndFlush(notificationSetting);

        // Get the notificationSetting
        restNotificationSettingMockMvc
            .perform(get(ENTITY_API_URL_ID, notificationSetting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(notificationSetting.getId().intValue()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED))
            .andExpect(jsonPath("$.fcmToken").value(DEFAULT_FCM_TOKEN));
    }

    @Test
    @Transactional
    void getNonExistingNotificationSetting() throws Exception {
        // Get the notificationSetting
        restNotificationSettingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingNotificationSetting() throws Exception {
        // Initialize the database
        insertedNotificationSetting = notificationSettingRepository.saveAndFlush(notificationSetting);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notificationSetting
        NotificationSetting updatedNotificationSetting = notificationSettingRepository.findById(notificationSetting.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedNotificationSetting are not directly saved in db
        em.detach(updatedNotificationSetting);
        updatedNotificationSetting.enabled(UPDATED_ENABLED).fcmToken(UPDATED_FCM_TOKEN);
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(updatedNotificationSetting);

        restNotificationSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notificationSettingDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationSettingDTO))
            )
            .andExpect(status().isOk());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedNotificationSettingToMatchAllProperties(updatedNotificationSetting);
    }

    @Test
    @Transactional
    void putNonExistingNotificationSetting() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notificationSetting.setId(longCount.incrementAndGet());

        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotificationSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, notificationSettingDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchNotificationSetting() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notificationSetting.setId(longCount.incrementAndGet());

        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationSettingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(notificationSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNotificationSetting() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notificationSetting.setId(longCount.incrementAndGet());

        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationSettingMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(notificationSettingDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateNotificationSettingWithPatch() throws Exception {
        // Initialize the database
        insertedNotificationSetting = notificationSettingRepository.saveAndFlush(notificationSetting);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notificationSetting using partial update
        NotificationSetting partialUpdatedNotificationSetting = new NotificationSetting();
        partialUpdatedNotificationSetting.setId(notificationSetting.getId());

        partialUpdatedNotificationSetting.fcmToken(UPDATED_FCM_TOKEN);

        restNotificationSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotificationSetting.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNotificationSetting))
            )
            .andExpect(status().isOk());

        // Validate the NotificationSetting in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNotificationSettingUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedNotificationSetting, notificationSetting),
            getPersistedNotificationSetting(notificationSetting)
        );
    }

    @Test
    @Transactional
    void fullUpdateNotificationSettingWithPatch() throws Exception {
        // Initialize the database
        insertedNotificationSetting = notificationSettingRepository.saveAndFlush(notificationSetting);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the notificationSetting using partial update
        NotificationSetting partialUpdatedNotificationSetting = new NotificationSetting();
        partialUpdatedNotificationSetting.setId(notificationSetting.getId());

        partialUpdatedNotificationSetting.enabled(UPDATED_ENABLED).fcmToken(UPDATED_FCM_TOKEN);

        restNotificationSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNotificationSetting.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNotificationSetting))
            )
            .andExpect(status().isOk());

        // Validate the NotificationSetting in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNotificationSettingUpdatableFieldsEquals(
            partialUpdatedNotificationSetting,
            getPersistedNotificationSetting(partialUpdatedNotificationSetting)
        );
    }

    @Test
    @Transactional
    void patchNonExistingNotificationSetting() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notificationSetting.setId(longCount.incrementAndGet());

        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNotificationSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, notificationSettingDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNotificationSetting() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notificationSetting.setId(longCount.incrementAndGet());

        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationSettingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(notificationSettingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNotificationSetting() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        notificationSetting.setId(longCount.incrementAndGet());

        // Create the NotificationSetting
        NotificationSettingDTO notificationSettingDTO = notificationSettingMapper.toDto(notificationSetting);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNotificationSettingMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(notificationSettingDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the NotificationSetting in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteNotificationSetting() throws Exception {
        // Initialize the database
        insertedNotificationSetting = notificationSettingRepository.saveAndFlush(notificationSetting);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the notificationSetting
        restNotificationSettingMockMvc
            .perform(delete(ENTITY_API_URL_ID, notificationSetting.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return notificationSettingRepository.count();
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

    protected NotificationSetting getPersistedNotificationSetting(NotificationSetting notificationSetting) {
        return notificationSettingRepository.findById(notificationSetting.getId()).orElseThrow();
    }

    protected void assertPersistedNotificationSettingToMatchAllProperties(NotificationSetting expectedNotificationSetting) {
        assertNotificationSettingAllPropertiesEquals(
            expectedNotificationSetting,
            getPersistedNotificationSetting(expectedNotificationSetting)
        );
    }

    protected void assertPersistedNotificationSettingToMatchUpdatableProperties(NotificationSetting expectedNotificationSetting) {
        assertNotificationSettingAllUpdatablePropertiesEquals(
            expectedNotificationSetting,
            getPersistedNotificationSetting(expectedNotificationSetting)
        );
    }
}
