package com.codeit.monew.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.Mockito.mock;

import com.codeit.monew.article.entity.Article;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

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

  @Mock
  private NotificationMapper notificationMapper;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  @DisplayName("관심사 관련 알림 생성 성공")
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
        .resourceType(ResourceType.interest)
        .resourceId(interestId)
        .build();

    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.createInterestArticleNotification(userId, interestId, articleCount);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo("[경제]와 관련된 기사가 3건 등록되었습니다.");
    assertThat(result.isConfirmed()).isFalse();
    assertThat(result.getResourceType()).isEqualTo(ResourceType.interest);
    assertThat(result.getResourceId()).isEqualTo(interestId);
  }

  @Test
  @DisplayName("사용자가 존재하지 않으면 관심사 알림 생성 시 UserException 발생")
  void createInterestNotification_userNotFound_shouldThrow() {
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    UserException ex = assertThrows(UserException.class, () ->
        notificationService.createInterestArticleNotification(userId, interestId, 3));

    assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("관심사가 존재하지 않으면 관심사 알림 생성 시 InterestException 발생")
  void createInterestNotification_interestNotFound_shouldThrow() {
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().build()));
    given(interestRepository.findById(interestId)).willReturn(Optional.empty());

    InterestException ex = assertThrows(InterestException.class, () ->
        notificationService.createInterestArticleNotification(userId, interestId, 3));

    assertThat(ex.getErrorCode()).isEqualTo(InterestErrorCode.INTEREST_NOT_FOUND);
  }

  @Test
  @DisplayName("댓글 좋아요 알림 생성 성공")
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
        .resourceType(ResourceType.comment)
        .resourceId(commentId)
        .build();

    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.createCommentLikeNotification(userId, commentId, likeByUserId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUser().getId()).isEqualTo(userId);
    assertThat(result.getResourceId()).isEqualTo(commentId);
    assertThat(result.getResourceType()).isEqualTo(ResourceType.comment);
    assertThat(result.getContent()).isEqualTo("[좋아요누른사람]님이 나의 댓글을 좋아합니다.");
  }

  @Test
  @DisplayName("존재하지 않는 댓글 좋아요 알림 생성 시 CommentException 발생")
  void createCommentLikeNotification_commentNotFound_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID likeByUserId = UUID.randomUUID();

    User user = User.builder().build();
    ReflectionTestUtils.setField(user, "id", userId);
    User likeByUser = User.builder().build();
    ReflectionTestUtils.setField(likeByUser, "id", likeByUserId);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.findById(likeByUserId)).willReturn(Optional.of(likeByUser));
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    // when & then
    CommentException ex = assertThrows(CommentException.class, () ->
        notificationService.createCommentLikeNotification(userId, commentId, likeByUserId));
    assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("댓글 작성자가 아니면 댓글 좋아요 알림 생성 시 CommentException 발생")
  void createCommentLikeNotification_notOwner_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID likeByUserId = UUID.randomUUID();

    User user = User.builder().nickname("작성자").build();
    ReflectionTestUtils.setField(user, "id", userId);
    User likeByUser = User.builder().nickname("좋아요누른사람").build();
    ReflectionTestUtils.setField(likeByUser, "id", likeByUserId);

    Comment comment = Comment.builder()
        .user(User.builder().id(UUID.randomUUID()).build()) // 다른 사용자
        .content("댓글")
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.findById(likeByUserId)).willReturn(Optional.of(likeByUser));
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when & then
    CommentException ex = assertThrows(CommentException.class, () ->
        notificationService.createCommentLikeNotification(userId, commentId, likeByUserId));
    assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.IDENTITY_VERIFICATION_EXCEPTION);
  }

  @Test
  @DisplayName("댓글 좋아요 알림 생성 시 userId가 존재하지 않으면 UserException 발생")
  void createCommentLikeNotification_userNotFound_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID likeByUserId = UUID.randomUUID();

    // user 조회가 실패
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException ex = assertThrows(UserException.class, () ->
        notificationService.createCommentLikeNotification(userId, commentId, likeByUserId));

    // then
    assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    assertThat(ex.getDetails()).containsEntry("userId", userId);
  }

  @Test
  @DisplayName("댓글 좋아요 알림 생성 시 likeByUserId가 존재하지 않으면 UserException 발생")
  void createCommentLikeNotification_likeByUserNotFound_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID likeByUserId = UUID.randomUUID();

    User user = User.builder().build();
    ReflectionTestUtils.setField(user, "id", userId);

    // user 조회 성공
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    // likeByUser 조회 실패
    given(userRepository.findById(likeByUserId)).willReturn(Optional.empty());

    // when & then
    UserException ex = assertThrows(UserException.class, () ->
        notificationService.createCommentLikeNotification(userId, commentId, likeByUserId));

    // then
    assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    assertThat(ex.getDetails()).containsEntry("likeByUserId", likeByUserId);
  }

  @Test
  @DisplayName("알림 단건 확인 성공")
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
        .resourceType(ResourceType.interest)
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
  @DisplayName("알림 단건 확인 시 알림이 없으면 NotificationException 발생")
  void confirmNotification_invalidUserOrNotification_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    // notification 없을 때
    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    // when & then
    NotificationException ex = assertThrows(NotificationException.class, () ->
        notificationService.confirmNotification(userId, notificationId));
    assertThat(ex.getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
  }

  @Test
  @DisplayName("알림 단건 확인 시 소유자가 다르면 NotificationException 발생")
  void confirmNotification_wrongOwner_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    User user = User.builder().id(UUID.randomUUID()).build();
    Notification notification = Notification.builder()
        .user(User.builder().id(UUID.randomUUID()).build()) // 다른 user
        .confirmed(false)
        .build();

    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    // when & then
    NotificationException ex = assertThrows(NotificationException.class, () ->
        notificationService.confirmNotification(userId, notificationId));
    assertThat(ex.getErrorCode()).isEqualTo(NotificationErrorCode.NOT_OWNER);
  }

  @Test
  @DisplayName("알림 단건 확인 시 userId 또는 notificationId가 null이면 NotificationException 발생")
  void confirmNotification_userIdOrNotificationIdNull_shouldThrow() {
    // userId가 null인 경우
    UUID notificationId = UUID.randomUUID();
    assertThrows(NotificationException.class, () ->
        notificationService.confirmNotification(null, notificationId));

    // notificationId가 null인 경우
    UUID userId = UUID.randomUUID();
    assertThrows(NotificationException.class, () ->
        notificationService.confirmNotification(userId, null));
  }

  @Test
  @DisplayName("알림 단건 확인 시 예기치 않은 예외 발생하면 INTERNAL_ERROR 발생")
  void confirmNotification_unexpectedException_shouldThrowInternalError() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    // repository.findById에서 런타임 예외 발생
    given(notificationRepository.findById(notificationId))
        .willThrow(new RuntimeException("DB connection error"));

    // when & then
    NotificationException ex = assertThrows(NotificationException.class, () ->
        notificationService.confirmNotification(userId, notificationId));

    assertThat(ex.getErrorCode()).isEqualTo(NotificationErrorCode.INTERNAL_ERROR);
    assertThat(ex.getDetails()).containsEntry("message", "DB connection error");
  }

  @Test
  @DisplayName("모든 미확인 알림 확인 성공")
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
        .resourceType(ResourceType.interest)
        .resourceId(UUID.randomUUID())
        .build();

    Notification notification2 = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("알림2")
        .resourceType(ResourceType.comment)
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
  @DisplayName("모든 미확인 알림 확인 시 사용자가 존재하지 않으면 NotificationException 발생")
  void confirmAllNotifications_userNotFound_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    NotificationException ex = assertThrows(NotificationException.class, () ->
        notificationService.confirmAllNotifications(userId));
    assertThat(ex.getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
  }

  @Test
  @DisplayName("모든 미확인 알림 확인 시 userId가 null이면 NotificationException 발생")
  void confirmAllNotifications_userIdNull_shouldThrow() {
    // given
    UUID userId = null;

    // when & then
    NotificationException ex = assertThrows(NotificationException.class, () ->
        notificationService.confirmAllNotifications(userId));
    assertThat(ex.getErrorCode()).isEqualTo(NotificationErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("모든 미확인 알림 확인 시 예기치 않은 예외 발생하면 INTERNAL_ERROR 발생")
  void confirmAllNotifications_internalError_shouldThrow() {
    // given
    UUID userId = UUID.randomUUID();
    User user = User.builder().email("test@example.com").nickname("테스터").password("password").build();
    ReflectionTestUtils.setField(user, "id", userId);

    // userRepository 정상 반환
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // notificationRepository에서 예외 발생하도록 설정
    given(notificationRepository.findByUserAndConfirmedFalse(user))
        .willThrow(new RuntimeException("DB 오류"));

    // when & then
    NotificationException ex = assertThrows(NotificationException.class, () ->
        notificationService.confirmAllNotifications(userId));

    assertThat(ex.getErrorCode()).isEqualTo(NotificationErrorCode.INTERNAL_ERROR);
    assertThat(ex.getDetails()).containsEntry("message", "DB 오류");
  }

  @Test
  @DisplayName("1주일 이상 지난 확인된 알림 삭제 성공")
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
        .resourceType(ResourceType.comment)
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
  @DisplayName("삭제 대상 확인된 알림이 없으면 아무것도 삭제하지 않음")
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
  @DisplayName("미확인 알림은 오래되어도 삭제하지 않음")
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
        .resourceType(ResourceType.comment)
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

  @Test
  @DisplayName("미확인 알림 조회 성공")
  void getUnconfirmedNotifications_success() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId1 = UUID.randomUUID();
    UUID notificationId2 = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    Notification n1 = Notification.builder()
        .id(notificationId1)
        .user(User.builder().id(userId).build())
        .confirmed(false)
        .content("첫 번째 알림")
        .resourceType(ResourceType.interest)
        .resourceId(UUID.randomUUID())
        .createdAt(now.minusMinutes(10))
        .build();

    Notification n2 = Notification.builder()
        .id(notificationId2)
        .user(User.builder().id(userId).build())
        .confirmed(false)
        .content("두 번째 알림")
        .resourceType(ResourceType.comment)
        .resourceId(UUID.randomUUID())
        .createdAt(now.minusMinutes(5))
        .build();

    List<Notification> notifications = List.of(n1, n2);

    given(notificationRepository.findUnconfirmedNotifications(userId, null, PageRequest.of(0, 11)))
        .willReturn(notifications);
    given(notificationRepository.countByUserIdAndConfirmedFalse(userId))
        .willReturn(2L);
    given(notificationMapper.toDto(n1)).willReturn(new NotificationDto(
        n1.getId(), n1.getCreatedAt(), n1.getUpdatedAt(), n1.isConfirmed(), n1.getUser().getId(),
        n1.getContent(), n1.getResourceType(), n1.getResourceId()
    ));
    given(notificationMapper.toDto(n2)).willReturn(new NotificationDto(
        n2.getId(), n2.getCreatedAt(), n2.getUpdatedAt(), n2.isConfirmed(), n2.getUser().getId(),
        n2.getContent(), n2.getResourceType(), n2.getResourceId()
    ));

    // when
    CursorPageResponseNotificationDto result = notificationService.getUnconfirmedNotifications(
        userId, null, 10
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(2);
    assertThat(result.nextCursor()).isEqualTo(n2.getCreatedAt().toString()); // String cursor
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("미확인 알림 조회 시 limit+1개 이상일 경우 hasNext=true")
  void getUnconfirmedNotifications_hasNext_true() {
    // given
    UUID userId = UUID.randomUUID();
    LocalDateTime after = null;
    int limit = 2;

    // Notification 3개 생성 → limit+1
    Notification n1 = Notification.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now().minusMinutes(3)).build();
    Notification n2 = Notification.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now().minusMinutes(2)).build();
    Notification n3 = Notification.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now().minusMinutes(1)).build();
    List<Notification> notifications = List.of(n1, n2, n3);

    given(notificationRepository.findUnconfirmedNotifications(userId, after, PageRequest.of(0, limit + 1)))
        .willReturn(notifications);

    given(notificationRepository.countByUserIdAndConfirmedFalse(userId)).willReturn(3L);
    given(notificationMapper.toDto(any(Notification.class))).willAnswer(invocation -> {
      Notification n = invocation.getArgument(0);
      return new NotificationDto(n.getId(), n.getCreatedAt(), n.getUpdatedAt(), n.isConfirmed(), null, null, null, null);
    });

    // when
    CursorPageResponseNotificationDto result = notificationService.getUnconfirmedNotifications(userId, after, limit);

    // then
    assertThat(result.content()).hasSize(limit); // subList 적용되어 limit개만 반환
    assertThat(result.hasNext()).isTrue();       // 3개 중 2개만 반환 → hasNext=true
    assertThat(result.nextCursor()).isEqualTo(n2.getCreatedAt().toString()); // 마지막 요소 n2
    assertThat(result.totalElements()).isEqualTo(3L);
  }

  @Test
  @DisplayName("미확인 알림이 없으면 nextCursor=null")
  void getUnconfirmedNotifications_emptyNotifications_shouldSetNextAfterNull() {
    // given
    UUID userId = UUID.randomUUID();
    LocalDateTime after = null;
    int limit = 5;

    // notifications 비어 있음
    given(notificationRepository.findUnconfirmedNotifications(userId, after, PageRequest.of(0, limit + 1)))
        .willReturn(List.of());
    given(notificationRepository.countByUserIdAndConfirmedFalse(userId)).willReturn(0L);

    // when
    CursorPageResponseNotificationDto result = notificationService.getUnconfirmedNotifications(userId, after, limit);

    // then
    assertThat(result.content()).isEmpty();       // 알림 없으므로 content 비어 있음
    assertThat(result.nextCursor()).isNull();    // nextAfter 없으므로 null
    assertThat(result.hasNext()).isFalse();
    assertThat(result.totalElements()).isEqualTo(0L);
  }

  @Test
  @DisplayName("미확인 알림 조회 시 nextAfter 존재하면 String cursor 반환")
  void getUnconfirmedNotifications_nextAfterNotNull_shouldReturnStringCursor() {
    // given
    UUID userId = UUID.randomUUID();
    int limit = 2;

    Notification n1 = Notification.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now().minusMinutes(2)).build();
    Notification n2 = Notification.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.now().minusMinutes(1)).build();
    List<Notification> notifications = List.of(n1, n2);

    given(notificationRepository.findUnconfirmedNotifications(userId, null, PageRequest.of(0, limit + 1)))
        .willReturn(notifications);
    given(notificationRepository.countByUserIdAndConfirmedFalse(userId)).willReturn(2L);
    given(notificationMapper.toDto(any(Notification.class))).willAnswer(invocation -> {
      Notification n = invocation.getArgument(0);
      return new NotificationDto(n.getId(), n.getCreatedAt(), n.getUpdatedAt(), n.isConfirmed(), null, null, null, null);
    });

    // when
    CursorPageResponseNotificationDto result = notificationService.getUnconfirmedNotifications(userId, null, limit);

    // then
    assertThat(result.nextCursor()).isEqualTo(n2.getCreatedAt().toString()); // 마지막 요소 n2
  }

}