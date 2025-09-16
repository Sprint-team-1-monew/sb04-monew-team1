package com.codeit.monew.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.SortDirection;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.likeRepository.CommentLikeQuerydslRepositoryImpl;
import com.codeit.monew.comment.repository.likeRepository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.repository.CommentRepositoryCustom;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.response_dto.CursorPageResponseCommentDto;
import com.codeit.monew.user.entity.User;
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

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @Mock
  private CommentLikeQuerydslRepositoryImpl commentLikeQuerydslRepositoryImpl;

  @Mock
  private CommentRepositoryCustom commentRepositoryCustom;

  @InjectMocks
  private CommentService commentService;

  @Mock
  private UserRepository userRepository;  // Mock 객체\

  @Mock
  private ArticleRepository articleRepository;

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
    verify(commentLikeQuerydslRepositoryImpl, times(1)).deleteByCommentId(commentId);
    verify(commentRepository, times(1)).delete(mappedEntity);

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

    Comment mappedEntity = Comment.builder()
        .id(UUID.randomUUID())
        .content("테스트 댓글")
        .createdAt(LocalDateTime.now())
        .user(dummyUser)
        .article(dummyArticle)
        .build();

    CommentDto dummyDto = new CommentDto(
        mappedEntity.getId(),               // UUID id
        mappedEntity.getArticle().getId(),  // UUID articleId
        mappedEntity.getUser().getId(),     // UUID userId
        mappedEntity.getUser().getNickname(), // String userNickname
        mappedEntity.getContent(),          // String content
        0L,                                 // Long likeCount
        false,                               // Boolean likedByMe
        mappedEntity.getCreatedAt()         // LocalDateTime createdAt
    );

    given(commentRepository.findComments(
        any(UUID.class),
        any(CommentOrderBy.class),
        any(SortDirection.class),
        anyString(),
        any(LocalDateTime.class),
        anyInt()
    )).willReturn(List.of(mappedEntity));

    given(commentMapper.toCommentDto(any(Comment.class), anyBoolean()))
        .willReturn(dummyDto);

    //when
    CursorPageResponseCommentDto result = commentService.getComments(articleId,
        CommentOrderBy.createdAt,
        SortDirection.DESC,
        "cursor-value",
        LocalDateTime.now(),
        10,
        requestUserId
    );

    //then
    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).content()).isEqualTo("테스트 댓글");
    assertThat(result.content().get(0).articleId()).isEqualTo(articleId);
    assertThat(result.hasNext()).isFalse();

  }

  @Test
  @DisplayName("댓글 좋아요 성공")
  void CommentLike_Success() {

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

    //given
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    //when
    commentService.commentLike(commentId, userId);

    //then
    assertThat(comment.getLikeCount()).isEqualTo(1);
    then(commentLikeRepository).should(times(1)).save(any(CommentLike.class));
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
}