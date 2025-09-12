package com.codeit.monew.notification.repository;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByUserAndConfirmedFalse(User user);

  List<Notification> findByConfirmedTrueAndUpdatedAtBefore(LocalDateTime weekAgo);

  @Query("""
    SELECT n FROM Notification n
    WHERE n.user.id = :userId
      AND n.confirmed = false
      AND (n.createdAt > :after OR (n.createdAt = :after AND n.id > :cursor))
    ORDER BY n.createdAt ASC, n.id ASC
""")
  List<Notification> findUnconfirmedNotifications(
      @Param("userId") UUID userId,
      @Param("after") LocalDateTime after,
      @Param("cursor") UUID cursor,
      Pageable pageable
  );

  Long countByUserIdAndConfirmedFalse(UUID userId);
}
