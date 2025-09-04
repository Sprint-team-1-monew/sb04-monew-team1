package com.codeit.monew.notification.service.basic;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.response_dto.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

  @Override
  public Notification createInterestArticleNotification(UUID userId, UUID interestId,
      int articleCount) {
    return null;
  }

  @Override
  public Notification createCommentLikeNotification(UUID userId, UUID commentId,
      UUID likeByUserId) {
    return null;
  }

  @Override
  public Notification confirmNotification(UUID userId, UUID notificationId) {
    return null;
  }

  @Override
  public void confirmAllNotifications(UUID userId) {

  }

  @Override
  public void deleteOldConfirmNotifications() {

  }

  @Override
  public CursorPageResponseNotificationDto getUnconfirmedNotifications(UUID userId, String cursor,
      LocalDateTime after, int limit) {
    return null;
  }
}
