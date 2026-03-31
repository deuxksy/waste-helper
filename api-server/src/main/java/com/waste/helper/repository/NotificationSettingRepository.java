package com.waste.helper.repository;

import com.waste.helper.domain.NotificationSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NotificationSetting entity.
 */
@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    @Query(
        "select notificationSetting from NotificationSetting notificationSetting where notificationSetting.user.login = ?#{authentication.name}"
    )
    List<NotificationSetting> findByUserIsCurrentUser();

    default Optional<NotificationSetting> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<NotificationSetting> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<NotificationSetting> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select notificationSetting from NotificationSetting notificationSetting left join fetch notificationSetting.user",
        countQuery = "select count(notificationSetting) from NotificationSetting notificationSetting"
    )
    Page<NotificationSetting> findAllWithToOneRelationships(Pageable pageable);

    @Query("select notificationSetting from NotificationSetting notificationSetting left join fetch notificationSetting.user")
    List<NotificationSetting> findAllWithToOneRelationships();

    @Query(
        "select notificationSetting from NotificationSetting notificationSetting left join fetch notificationSetting.user where notificationSetting.id =:id"
    )
    Optional<NotificationSetting> findOneWithToOneRelationships(@Param("id") Long id);
}
