package com.codeit.monew.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.entity.ResourceType;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(properties = {
    "spring.task.scheduling.enabled=false",
    "spring.quartz.job-store-type=memory",
    "spring.quartz.auto-startup=false"
})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private CommentRepository commentRepository;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  void createInterestNotification_success(){
    // given
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();
    int articleCount = 3;

    User user = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .password("password")
        .build();

    Interest interest = Interest.builder()
        .name("경제")
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("[경제]와 관련된 기사가 3건 등록되었습니다.")
        .resourceType(ResourceType.INTEREST)
        .resourceId(interestId)
        .build();

    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.createInterestArticleNotification(userId, interestId, articleCount);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo("[경제]와 관련된 기사가 3건 등록되었습니다.");
    assertThat(result.isConfirmed()).isFalse();
    assertThat(result.getResourceType()).isEqualTo(ResourceType.INTEREST);
    assertThat(result.getResourceId()).isEqualTo(interestId);
  }

  @Test
  void createCommentLikeNotification_success(){
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID likeByUserId = UUID.randomUUID();

    User user = User.builder()
        .email("author@test.com")
        .nickname("작성자")
        .password("password")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);

    User likeByUser = User.builder()
        .email("liker@test.com")
        .nickname("좋아요누른사람")
        .password("password")
        .build();
    ReflectionTestUtils.setField(likeByUser, "id", likeByUserId);

    Comment comment = Comment.builder()
        .content("댓글")
        .isDeleted(false)
        .likeCount(0)
        .user(user)
        .article(mock(Article.class))
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.findById(likeByUserId)).willReturn(Optional.of(likeByUser));
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("[좋아요누른사람]님이 나의 댓글을 좋아합니다.")
        .resourceType(ResourceType.COMMENT)
        .resourceId(commentId)
        .build();

    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.createCommentLikeNotification(userId, commentId, likeByUserId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUser().getId()).isEqualTo(userId);
    assertThat(result.getResourceId()).isEqualTo(commentId);
    assertThat(result.getResourceType()).isEqualTo(ResourceType.COMMENT);
    assertThat(result.getContent()).isEqualTo("[좋아요누른사람]님이 나의 댓글을 좋아합니다.");
  }

  @Test
  void confirmNotification_success() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    User user = User.builder()
        .email("user@test.com")
        .nickname("테스터")
        .password("password")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);

    Notification notification = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("알림")
        .resourceType(ResourceType.INTEREST)
        .resourceId(UUID.randomUUID())
        .build();
    ReflectionTestUtils.setField(notification, "id", notificationId);

    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.confirmNotification(userId, notificationId);

    // then
    assertThat(result.isConfirmed()).isTrue();
    assertThat(result.getId()).isEqualTo(notificationId);
    assertThat(result.getUser().getId()).isEqualTo(userId);
  }

  @Test
  void confirmAllNotifications_success() {
    // given
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .password("password")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);

    Notification notification1 = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("알림1")
        .resourceType(ResourceType.INTEREST)
        .resourceId(UUID.randomUUID())
        .build();

    Notification notification2 = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("알림2")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();

    List<Notification> notifications = List.of(notification1, notification2);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(notificationRepository.findByUserAndConfirmedFalse(user)).willReturn(notifications);
    given(notificationRepository.saveAll(notifications)).willReturn(notifications);

    // when
    notificationService.confirmAllNotifications(userId);

    // then
    notifications.forEach(notification -> assertThat(notification.isConfirmed()).isTrue());
  }

  @Test
  void should_deleteOldConfirmedNotifications_when_theyExist() {
    // given
    LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

    User user = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .password("password")
        .build();

    Notification oldConfirmed = Notification.builder()
        .user(user)
        .confirmed(true) // 삭제 대상
        .content("알림1")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .updatedAt(weekAgo.minusDays(1)) // 1주일보다 오래됨
        .build();

    List<Notification> notifications = List.of(oldConfirmed);

    given(notificationRepository.findByConfirmedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
        .willReturn(notifications);

    // when
    notificationService.deleteOldConfirmNotifications();

    // then
    then(notificationRepository).should(times(1)).deleteAll(notifications);
  }

  @Test
  void should_notDeleteAnything_when_noOldConfirmedNotificationsExist() {
    // given
    given(notificationRepository.findByConfirmedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
        .willReturn(List.of());

    // when
    notificationService.deleteOldConfirmNotifications();

    // then
    then(notificationRepository).should(never()).deleteAll(anyList());
  }

  @Test
  void should_notDeleteUnconfirmedNotifications_evenIfOld() {
    // given
    LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

    User user = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .password("password")
        .build();

    Notification unconfirmed = Notification.builder()
        .user(user)
        .confirmed(false) // 삭제 대상 아님
        .content("알림2")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .updatedAt(weekAgo.minusDays(2))
        .build();

    // repository는 confirmed=true 인 알림만 반환하므로 빈 리스트
    given(notificationRepository.findByConfirmedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
        .willReturn(List.of());

    // when
    notificationService.deleteOldConfirmNotifications();

    // then
    then(notificationRepository).should(never()).deleteAll(anyList());
  }
}
