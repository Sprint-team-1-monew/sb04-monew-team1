package com.codeit.monew.notification.repository;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByUserAndConfirmedFalse(User user);

  List<Notification> findByConfirmedTrueAndUpdatedAtBefore(LocalDateTime weekAgo);

  //테스트용
  @Modifying
  @Transactional // update 쿼리 실행에 필요한 트랜잭션
  @Query("update Notification n set n.updatedAt = :updatedAt where n.id = :id")
  void updateUpdatedAt(@Param("id") UUID id, @Param("updatedAt") LocalDateTime updatedAt);
}
