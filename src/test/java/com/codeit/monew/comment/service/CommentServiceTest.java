package com.codeit.monew.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.SortDirection;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.repository.likeRepository.CommentLikeRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.response_dto.CursorPageResponseCommentDto;
import com.codeit.monew.exception.article.ArticleErrorCode;
import com.codeit.monew.exception.article.ArticleException;
import com.codeit.monew.exception.comment.CommentErrorCode;
import com.codeit.monew.exception.comment.CommentException;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @InjectMocks
  private CommentService commentService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private ApplicationEventPublisher publisher;

  private UUID userId;
  private UUID articleId;
  private UUID commentId;
  private CommentRegisterRequest commentRegisterRequest;
  private CommentUpdateRequest commentUpdateRequest;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    articleId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    commentRegisterRequest = new CommentRegisterRequest(userId, articleId, "테스트 댓글 입니다.");
    commentUpdateRequest = new CommentUpdateRequest("수정 된 내용");
  }


  @Test
  @DisplayName("댓글 등록 성공")
  void createComment_Success() {
    //given
    Comment mappedEntity = Comment.builder()
        .content(commentRegisterRequest.content())
        .isDeleted(false)
        .likeCount(0)
        .user(User.builder().build())
        .article(Article.builder().build())
        .build();

    Comment savedEntity = mappedEntity; // 간단히 동일 객체 사용

    CommentDto expectedDto = new CommentDto(
        UUID.randomUUID(),
        articleId,
        userId,
        "tester",
        commentRegisterRequest.content(),
        0L,
        false,
        LocalDateTime.now()
    );

    User mockUser = User.builder()
        .id(userId)
        .nickname("tester")
        .build();

    Article mockArticle = Article.builder()
        .id(articleId)
        .build();

    given(userRepository.findById(any())).willReturn(Optional.of(mockUser));
    given(articleRepository.findById(any())).willReturn(Optional.of(mockArticle));
    given(commentMapper.toCommentEntity(commentRegisterRequest)).willReturn(mappedEntity);
    given(commentRepository.save(mappedEntity)).willReturn(savedEntity);
    given(commentMapper.toCommentDto(savedEntity, expectedDto.likedByMe())).willReturn(expectedDto);

    // when
    CommentDto result = commentService.createComment(commentRegisterRequest);

    // then
    assertNotNull(result);
    then(commentMapper).should().toCommentEntity(commentRegisterRequest);
    then(commentRepository).should().save(mappedEntity);
    then(commentMapper).should().toCommentDto(savedEntity, expectedDto.likedByMe());
  }

  @Test
  @DisplayName("댓글 등록 실패 - 유저가 존재하지 않을 때")
  void createComment_Fail_UserNotFound() {
    //given
    given(userRepository.findById(commentRegisterRequest.userId())).willReturn(Optional.empty()); // 무조건 empty 반환

    //when & then
    UserException exception = assertThrows(UserException.class, () -> commentService.createComment(commentRegisterRequest));

    assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());

    // 검증 : articleRepository 와 commentRepository 는 호출되지 않아야 한다.
    verify(articleRepository, never()).findById(any());
    verify(commentRepository, never()).save(any());

  }

  @Test
  @DisplayName(("댓글 등록 실패 - 기사가 존재하지 않을 때"))
  void createComment_Fail_ArticleNotFound() {
    //given
    User mockUser = User.builder()
        .id(userId)
        .build();

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(mockUser));
    given(articleRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    ArticleException exception = assertThrows(ArticleException.class, () -> commentService.createComment(commentRegisterRequest));

    assertEquals(ArticleErrorCode.ARTICLE_NOT_FOUND_EXCEPTION, exception.getErrorCode());

    verify(commentRepository, never()).findById(any());
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void commentUpdate_Success() {
    //given 더미 User/Article 생성
    User dummyUser = User.builder()
        .id(userId)
        .nickname("tester")
        .build();

    Article dummyArticle = Article.builder()
        .id(articleId)
        .build();

    Comment mappedEntity = Comment.builder()
        .content(commentRegisterRequest.content())
        .isDeleted(false)
        .likeCount(0)
        .user(dummyUser)
        .article(dummyArticle)
        .build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(mappedEntity));
    given(commentRepository.save(any(Comment.class))).willAnswer(
        invocation -> invocation.getArgument(0));
    given(commentMapper.toCommentDto(any(Comment.class), eq(false)))
        .willReturn(new CommentDto(
            commentId,
            articleId,
            userId,
            "테스트유저",
            "수정된 내용",
            0L,
            false,
            LocalDateTime.now()
        ));

    // when
    CommentDto result = commentService.updateComment(commentId, userId, commentUpdateRequest);

    // then
    assertThat(result.content()).isEqualTo("수정된 내용");
    verify(commentRepository).findById(commentId);
    verify(commentRepository).save(any(Comment.class));
    then(commentMapper).should().toCommentDto(any(Comment.class), eq(false));

  }

  @Test
  @DisplayName("댓글 수정 실패 - 댓글이 존재하지 않음")
  void commentUpdate_Fail_CommentNotFound() {
    //given
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정된 내용");

    //commentRepository.findById() 호출 했을 때 Optional.empty() 반환
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    //when & then 예외 발생해야함
    assertThrows(CommentException.class, () -> commentService.updateComment(commentId, userId, updateRequest));

  }

  @Test
  @DisplayName("댓글 수정 실패 - 댓글 작성자가 아님")
  void commentUpdate_Fail_IdentityVerification() {
    //given
    UUID realAuthorId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정된 내용");

    //댓글 엔티티를 가짜로 생성 (작성자는 realAuthorId)
    Comment comment = Comment.builder()
        .content(commentRegisterRequest.content())
        .isDeleted(false)
        .likeCount(0)
        .user(User.builder().id(realAuthorId).build())
        .article(Article.builder().build())
        .build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    //when & then 작성자가 아니므로 예외 발생해야함
    CommentException exception = assertThrows(CommentException.class, () -> commentService.updateComment(commentId, requestId, updateRequest));

    assertEquals(CommentErrorCode.IDENTITY_VERIFICATION_EXCEPTION, exception.getErrorCode());

  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void softDelete_Success() {

    //given
    Comment mappedEntity = Comment.builder()
        .content(commentRegisterRequest.content())
        .isDeleted(false)
        .likeCount(0)
        .user(User.builder().build())
        .article(Article.builder().build())
        .build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(mappedEntity));

    //when
    commentService.softDeleteComment(commentId);

    //then
    ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository, times(1)).save(captor.capture());

    Comment savedComment = captor.getValue();
    assertThat(savedComment.getIsDeleted()).isTrue();
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패 - 댓글을 찾을 수 없음")
  void softDelete_Fail_CommentNotFound() {
    //given
    UUID commentId = UUID.randomUUID();
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    CommentException exception = assertThrows(CommentException.class,
        () -> commentService.softDeleteComment(commentId));

    assertEquals(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION, exception.getErrorCode());

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공")
  void hardDelete_Success() {

    //given
    Comment mappedEntity = Comment.builder()
        .content(commentRegisterRequest.content())
        .isDeleted(false)
        .likeCount(0)
        .user(User.builder().build())
        .article(Article.builder().build())
        .build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(mappedEntity));

    //when
    commentService.hardDeleteComment(commentId);

    //then
    verify(commentLikeRepository, times(1)).deleteByCommentId(eq(commentId));
    verify(commentRepository, times(1)).delete(mappedEntity);

  }

  @Test
  @DisplayName("댓글 물리 삭제 실패 - 댓글이 없는 경우")
  void hardDelete_Failure_CommentNotFound() {

    // given
    UUID commentId = UUID.randomUUID();
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    // when & then
    CommentException exception = assertThrows(CommentException.class, () -> {
      commentService.hardDeleteComment(commentId);
    });

    // 예외 코드 검증
    assertEquals(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION, exception.getErrorCode());

    // Repository 호출 검증: 댓글 없으니 delete 메서드는 호출되지 않아야 함
    verify(commentLikeRepository, never()).deleteByCommentId(any(UUID.class));
    verify(commentRepository, never()).delete(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 목록 조회 성공")
  void getComments_Success() {

    //given
    UUID articleId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    //더미 User, Article 생성
    User dummyUser = User.builder()
        .id(UUID.randomUUID())
        .nickname("tester")
        .build();

    Article dummyArticle = Article.builder()
        .id(articleId)
        .articleTitle("테스트 기사")
        .build();

    Comment comment1 = Comment.builder()
        .id(UUID.randomUUID())
        .content("테스트 댓글 1")
        .createdAt(LocalDateTime.now().minusMinutes(1))
        .isDeleted(false) // 삭제되지 않은 댓글
        .user(dummyUser)
        .article(dummyArticle)
        .build();

    Comment comment2 = Comment.builder()
        .id(UUID.randomUUID())
        .content("테스트 댓글 2")
        .createdAt(LocalDateTime.now())
        .isDeleted(true) // 삭제된 댓글 → filter 커버
        .user(dummyUser)
        .article(dummyArticle)
        .build();

    CommentDto dummyDto = new CommentDto(
        comment1.getId(),               // UUID id
        comment1.getArticle().getId(),  // UUID articleId
        comment1.getUser().getId(),     // UUID userId
        comment1.getUser().getNickname(), // String userNickname
        comment1.getContent(),          // String content
        0L,                                 // Long likeCount
        false,                               // Boolean likedByMe
        comment1.getCreatedAt()         // LocalDateTime createdAt
    );

    List<Comment> commentList = List.of(comment1, comment2);

    given(commentRepository.findComments(
        eq(articleId),
        eq(CommentOrderBy.createdAt),
        eq(SortDirection.DESC),
        eq(""),
        isNull(),
        eq(1)
    )).willReturn(commentList);

    given(commentMapper.toCommentDto(any(Comment.class), anyBoolean()))
        .willReturn(dummyDto);

    //when
    CursorPageResponseCommentDto result = commentService.getComments(articleId,
        CommentOrderBy.createdAt,
        SortDirection.DESC,
        "",
        null,
        1,
        requestUserId
    );

    //then
    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(1); // 삭제되지 않은 댓글만 포함
    assertThat(result.content().get(0).content()).isEqualTo("테스트 댓글 1");
    assertThat(result.content().get(0).articleId()).isEqualTo(articleId);
    assertThat(result.hasNext()).isTrue();       // [추가] hasNext 조건
    assertThat(result.nextCursor()).isNotNull(); // [추가] nextCursor 존재 확인
    assertThat(result.nextAfter()).isNotNull();  // [추가] nextAfter 존재 확인
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - 존재하지 않는 기사")
  void getComments_Fail_ArticleNotFound() {
    // given
    UUID articleId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    // commentRepository.findComments 호출 시 빈 리스트 반환
    given(commentRepository.findComments(
        articleId,
        CommentOrderBy.createdAt,
        SortDirection.DESC,
        "",
        null,
        10
    )).willReturn(List.of());

    // commentRepository.countByArticleId 호출 시 0 반환
    given(commentRepository.countByArticleId(articleId)).willReturn(0L);

    // when
    CursorPageResponseCommentDto result = commentService.getComments(
        articleId,
        CommentOrderBy.createdAt,
        SortDirection.DESC,
        "",
        null,
        10,
        requestUserId
    );

    // then - 결과 검증
    assertEquals(0, result.content().size());      // 댓글 수 0
    assertEquals(0L, result.totalElements());     // 전체 댓글 수 0
    assertTrue(result.content().isEmpty());       // 리스트 비었는지 확인
    assertFalse(result.hasNext());                // 다음 페이지 없음을 확인

    // then - repository 호출 검증
    verify(commentRepository, times(1))
        .findComments(eq(articleId), any(), any(), anyString(), any(), anyInt());
    verify(commentRepository, times(2)).countByArticleId(articleId);
  }

  @Test
  @DisplayName("댓글 좋아요 성공")
  void CommentLike_Success() {

    //given
    Article article = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("테스트 글")
        .build();

    User user = User.builder()
        .id(userId)
        .nickname("테스트유저")
        .build();

    Comment comment = Comment.builder()
        .id(commentId)
        .content("테스트 댓글")
        .article(article)
        .user(user)
        .likeCount(0)
        .build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    //when
    commentService.commentLike(commentId, userId);

    //then
    assertThat(comment.getLikeCount()).isEqualTo(1);
    then(commentLikeRepository).should(times(1)).save(any(CommentLike.class));
  }

  @Test
  @DisplayName("댓글 좋아요 실패 - 댓글이 존재하지 않음")
  void commentLike_Fail_CommentNotFound() {
    //given
    UUID commentId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    // commentRepository.findById 호출 시 빈 Optional 반환 → 댓글 없음
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    // 실행 시 CommentException 발생 확인
    CommentException exception = assertThrows(CommentException.class,
        () -> commentService.commentLike(commentId, requestUserId));

    assertEquals(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION, exception.getErrorCode());

    // Repository 호출 검증
    verify(commentRepository, times(1)).findById(commentId);
    verifyNoMoreInteractions(commentRepository);
  }

  @Test
  @DisplayName("댓글 좋아요 실패 - 사용자가 존재하지 않음")
  void commentLike_Fail_UserNotFound() {
    UUID commentId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    // 댓글 존재
    Comment comment = Comment.builder().id(commentId).likeCount(0).build();
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // userRepository.findById 호출 시 빈 Optional 반환 → 사용자 없음
    given(userRepository.findById(requestUserId)).willReturn(Optional.empty());

    // 실행 시 UserException 발생 확인
    UserException exception = assertThrows(UserException.class,
        () -> commentService.commentLike(commentId, requestUserId));

    assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());

    verify(commentRepository, times(1)).findById(commentId);
    verify(userRepository, times(1)).findById(requestUserId);
    verifyNoMoreInteractions(commentRepository, userRepository);
  }

  @Test
  @DisplayName("댓글 좋아요 실패 - 이미 좋아요 눌림")
  void commentLike_Fail_DuplicateLikes() {
    UUID commentId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    // 댓글과 사용자 존재
    Comment comment = Comment.builder().id(commentId).likeCount(0).build();
    User user = User.builder().id(requestUserId).build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(requestUserId)).willReturn(Optional.of(user));

    // 이미 좋아요 눌린 경우 true 반환
    given(commentLikeRepository.existsByCommentIdAndUserId(commentId, requestUserId)).willReturn(true);

    // 실행 시 CommentException 발생 확인
    CommentException exception = assertThrows(CommentException.class,
        () -> commentService.commentLike(commentId, requestUserId));

    assertEquals(CommentErrorCode.DUPLICATE_LIKES, exception.getErrorCode());

    verify(commentRepository, times(1)).findById(commentId);
    verify(userRepository, times(1)).findById(requestUserId);
    verify(commentLikeRepository, times(1)).existsByCommentIdAndUserId(commentId, requestUserId);
    verifyNoMoreInteractions(commentRepository, userRepository, commentLikeRepository);
  }

  @Test
  @DisplayName("댓글 좋아요 취소 성공")
  void CommentLike_cancel() {

    Article article = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("테스트 글")
        .build();

    User user = User.builder()
        .id(userId)
        .nickname("테스트유저")
        .build();

    Comment comment = Comment.builder()
        .id(commentId)
        .content("테스트 댓글")
        .article(article)
        .user(user)
        .likeCount(0)
        .build();

    // given
    CommentLike commentLike = CommentLike.builder()
        .comment(comment)
        .user(user)
        .build();
    comment.setLikeCount(1);

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(commentLikeRepository.findByCommentAndUser(comment, user))
        .willReturn(Optional.of(commentLike));

    // when
    commentService.deleteCommentLike(commentId, userId);

    // then
    assertThat(comment.getLikeCount()).isEqualTo(0);
    then(commentLikeRepository).should(times(1)).delete(commentLike);

  }
  @Test
  @DisplayName("댓글 좋아요 취소 실패 - 댓글이 존재하지 않음")
  void deleteCommentLike_Fail_CommentNotFound() {
    UUID commentId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    //given 댓글 없음
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());


    //when
    CommentException exception = assertThrows(CommentException.class,
        () -> commentService.deleteCommentLike(commentId, requestUserId));

    //then
    assertEquals(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION, exception.getErrorCode());

    verify(commentRepository, times(1)).findById(commentId);
    verifyNoMoreInteractions(commentRepository);
  }

  @Test
  @DisplayName("댓글 좋아요 취소 실패 - 사용자 존재하지 않음")
  void deleteCommentLike_Fail_UserNotFound() {
    UUID commentId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    //given
    Comment comment = Comment.builder().id(commentId).likeCount(1).build();
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(requestUserId)).willReturn(Optional.empty());

    //when
    UserException exception = assertThrows(UserException.class,
        () -> commentService.deleteCommentLike(commentId, requestUserId));

    assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());

    //then
    verify(commentRepository, times(1)).findById(commentId);
    verify(userRepository, times(1)).findById(requestUserId);
    verifyNoMoreInteractions(commentRepository, userRepository);
  }

  @Test
  @DisplayName("댓글 좋아요 취소 실패 - 댓글에 좋아요 없음")
  void deleteCommentLike_Fail_CommentNotLiked() {
    UUID commentId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    //given
    Comment comment = Comment.builder().id(commentId).likeCount(1).build();
    User user = User.builder().id(requestUserId).build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(requestUserId)).willReturn(Optional.of(user));

    // 댓글 좋아요 없음
    given(commentLikeRepository.findByCommentAndUser(comment, user)).willReturn(Optional.empty());

    //when
    CommentException exception = assertThrows(CommentException.class,
        () -> commentService.deleteCommentLike(commentId, requestUserId));

    //then
    assertEquals(CommentErrorCode.COMMENT_NOT_LIKE, exception.getErrorCode());

    verify(commentRepository, times(1)).findById(commentId);
    verify(userRepository, times(1)).findById(requestUserId);
    verify(commentLikeRepository, times(1)).findByCommentAndUser(comment, user);
    verifyNoMoreInteractions(commentRepository, userRepository, commentLikeRepository);
  }

}