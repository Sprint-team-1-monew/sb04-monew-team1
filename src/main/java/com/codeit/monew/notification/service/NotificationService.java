package com.codeit.monew.notification.service;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.response_dto.CursorPageResponseNotificationDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
  Notification createInterestArticleNotification(UUID userId, UUID interestId, int articleCount);

  Notification createCommentLikeNotification(UUID userId, UUID commentId, UUID likeByUserId);

  Notification confirmNotification(UUID userId, UUID notificationId);

  void confirmAllNotifications(UUID userId);

  void deleteOldConfirmNotifications();

  CursorPageResponseNotificationDto getUnconfirmedNotifications(UUID userId, String cursor, LocalDateTime after, int limit);
}
