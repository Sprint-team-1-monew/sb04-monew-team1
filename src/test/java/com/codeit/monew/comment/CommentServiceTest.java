package com.codeit.monew.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeQuerydslRepository;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.service.CommentService;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import kotlin.DslMarker;
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
  private CommentLikeQuerydslRepository commentLikeQuerydslRepository;

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

    given(userRepository.existsById(any())).willReturn(true);
    given(articleRepository.existsById(any())).willReturn(true);
    given(commentMapper.toCommentEntity(commentRegisterRequest)).willReturn(mappedEntity);
    given(commentRepository.save(mappedEntity)).willReturn(savedEntity);
    given(commentMapper.toCommentDto(savedEntity)).willReturn(expectedDto);

    // when
    CommentDto result = commentService.createComment(commentRegisterRequest);

    // then
    assertNotNull(result);
    then(commentMapper).should().toCommentEntity(commentRegisterRequest);
    then(commentRepository).should().save(mappedEntity);
    then(commentMapper).should().toCommentDto(savedEntity);
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
    given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));
    given(commentMapper.toCommentDto(any(Comment.class)))
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
    verify(commentRepository).save(mappedEntity);
    then(commentMapper).should().toCommentDto(any(Comment.class));

  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void softDelete_Success(){

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
  void hardDelete_Success(){

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
    verify(commentLikeQuerydslRepository, times(1)).deleteByCommentId(commentId);
    verify(commentRepository, times(1)).delete(mappedEntity);

  }

}
