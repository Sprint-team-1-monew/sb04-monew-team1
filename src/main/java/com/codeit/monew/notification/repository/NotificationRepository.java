package com.codeit.monew.notification.repository;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByUserAndConfirmedFalse(User user);
}