package com.codeit.monew.notification.service;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.entity.ResourceType;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.notification.response_dto.CursorPageResponseNotificationDto;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final NotificationRepository notificationRepository;
  private final CommentRepository commentRepository;

  public Notification createInterestArticleNotification(UUID userId,
      UUID interestId,
      int articleCount) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new EntityNotFoundException("Interest not found"));

    String message = String.format("[%s]와 관련된 기사가 %d건 등록되었습니다.",
        interest.getName(), articleCount);

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content(message)
        .resourceType(ResourceType.INTEREST)
        .resourceId(interestId)
        .build();

    return notificationRepository.save(notification);
  }

  public Notification createCommentLikeNotification(UUID userId,
      UUID commentId,
      UUID likeByUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    User likeByUser = userRepository.findById(likeByUserId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

    if (!comment.getUser().getId().equals(user.getId())) {
      throw new IllegalArgumentException("해당 댓글은 이 사용자의 댓글이 아닙니다.");
    }

    String message = String.format("[%s]님이 나의 댓글을 좋아합니다.", likeByUser.getNickname());

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content(message)
        .resourceType(ResourceType.COMMENT)
        .resourceId(commentId)
        .build();

    return notificationRepository.save(notification);
  }

  public Notification confirmNotification(UUID userId, UUID notificationId) {
    return null;
  }

  public void confirmAllNotifications(UUID userId) {

  }

  public void deleteOldConfirmNotifications() {

  }

  public CursorPageResponseNotificationDto getUnconfirmedNotifications(UUID userId, String cursor,
      LocalDateTime after, int limit) {
    return null;
  }
}
