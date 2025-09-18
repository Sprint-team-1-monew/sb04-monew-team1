package com.codeit.monew.notification.service;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.exception.comment.CommentErrorCode;
import com.codeit.monew.exception.comment.CommentException;
import com.codeit.monew.exception.interest.InterestErrorCode;
import com.codeit.monew.exception.interest.InterestException;
import com.codeit.monew.exception.notification.NotificationErrorCode;
import com.codeit.monew.exception.notification.NotificationException;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.entity.ResourceType;
import com.codeit.monew.notification.mapper.NotificationMapper;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.notification.response_dto.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.response_dto.NotificationDto;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final NotificationRepository notificationRepository;
  private final CommentRepository commentRepository;
  private final NotificationMapper notificationMapper;

  public Notification createInterestArticleNotification(UUID userId,
      UUID interestId,
      int articleCount) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));

    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestException(
            InterestErrorCode.INTEREST_NOT_FOUND, Map.of("interestId", interestId)));

    String message = String.format("[%s]와 관련된 기사가 %d건 등록되었습니다.",
        interest.getName(), articleCount);

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content(message)
        .resourceType(ResourceType.interest)
        .resourceId(interestId)
        .build();

    return notificationRepository.save(notification);
  }

  public Notification createCommentLikeNotification(UUID userId,
      UUID commentId,
      UUID likeByUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));

    User likeByUser = userRepository.findById(likeByUserId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("likeByUserId", likeByUserId)));

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION, Map.of("commentId", commentId)));

    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CommentException(CommentErrorCode.IDENTITY_VERIFICATION_EXCEPTION,
          Map.of("userId", userId, "commentId", commentId));
    }

    String message = String.format("[%s]님이 나의 댓글을 좋아합니다.", likeByUser.getNickname());

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content(message)
        .resourceType(ResourceType.comment)
        .resourceId(commentId)
        .build();

    return notificationRepository.save(notification);
  }

  public Notification confirmNotification(UUID userId, UUID notificationId) {

    if (userId == null || notificationId == null) {
      throw new NotificationException(
          NotificationErrorCode.INVALID_REQUEST,
          Map.of("userId", userId != null ? userId : "null", "notificationId", notificationId != null ? notificationId : "null")
      );
    }

    try {
      Notification notification = notificationRepository.findById(notificationId)
          .orElseThrow(() -> new NotificationException(
              NotificationErrorCode.NOTIFICATION_NOT_FOUND,
              Map.of("notificationId", notificationId)
          ));

      if (!notification.getUser().getId().equals(userId)) {
        throw new NotificationException(
            NotificationErrorCode.NOT_OWNER,
            Map.of("userId", userId, "notificationUserId", notification.getUser().getId())
        );
      }

      notification.setConfirmed(true);
      return notificationRepository.save(notification);

    } catch (NotificationException ne) {
      throw ne;
    } catch (Exception e) {
      throw new NotificationException(
          NotificationErrorCode.INTERNAL_ERROR,
          Map.of("message", e.getMessage())
      );
    }
  }

  public void confirmAllNotifications(UUID userId) {
    if (userId == null) {
      throw new NotificationException(
          NotificationErrorCode.INVALID_REQUEST,
          Map.of("userId", "null")
      );
    }

    try {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new NotificationException(
              NotificationErrorCode.NOTIFICATION_NOT_FOUND,
              Map.of("userId", userId)
          ));

      List<Notification> notifications = notificationRepository.findByUserAndConfirmedFalse(user);
      notifications.forEach(notification -> notification.setConfirmed(true));
      notificationRepository.saveAll(notifications);

    } catch (NotificationException ne) {
      throw ne;
    } catch (Exception e) {
      throw new NotificationException(
          NotificationErrorCode.INTERNAL_ERROR,
          Map.of("message", e.getMessage())
      );
    }
  }

  public int deleteOldConfirmNotifications() {
    LocalDateTime weekAgo = LocalDateTime.now().minusDays(7).withNano(0);

    List<Notification> confirmedNotifications =
        notificationRepository.findByConfirmedTrueAndUpdatedAtBefore(weekAgo);

    if (!confirmedNotifications.isEmpty()) {
      notificationRepository.deleteAll(confirmedNotifications);
      return confirmedNotifications.size();
    }
    return 0;
  }

  @Transactional(readOnly = true)
  public CursorPageResponseNotificationDto getUnconfirmedNotifications(
      UUID userId,
      LocalDateTime after,
      int limit
  ) {

    List<Notification> notifications = notificationRepository.findUnconfirmedNotifications(
        userId,
        after,
        PageRequest.of(0, limit + 1)
    );

    boolean hasNext = notifications.size() > limit;

    if (hasNext) {
      notifications = notifications.subList(0, limit);
    }

    List<NotificationDto> content = notifications.stream()
        .map(notificationMapper::toDto)
        .toList();

    LocalDateTime nextAfter = null;
    if (!notifications.isEmpty()) {
      nextAfter = notifications.get(notifications.size() - 1).getCreatedAt();
    }

    Long totalElements = notificationRepository.countByUserIdAndConfirmedFalse(userId);

    return new CursorPageResponseNotificationDto(
        content,
        nextAfter != null ? nextAfter.toString() : null,
        nextAfter,
        limit,
        totalElements,
        hasNext
    );
  }
}
