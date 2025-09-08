package com.codeit.monew.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.service.CommentService;
import com.codeit.monew.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentMapper commentMapper;

  @InjectMocks
  private CommentService commentService;

  private UUID userId;
  private UUID articleId;
  private CommentRegisterRequest request;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    articleId = UUID.randomUUID();
    request = new CommentRegisterRequest(userId, articleId, "테스트 댓글 입니다.");
  }


  @Test
  @DisplayName("댓글 등록 성공")
  void createComment_Success() {
    //given

    Comment mappedEntity = Comment.builder()
        .content(request.content())
        .deleted(false)
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
        request.content(),
        0L,
        false,
        LocalDateTime.now()
    );

    when(commentMapper.toCommentEntity(request)).thenReturn(mappedEntity);
    when(commentRepository.save(mappedEntity)).thenReturn(savedEntity);
    when(commentMapper.toCommentDto(savedEntity)).thenReturn(expectedDto);

    // when
    CommentDto result = commentService.createComment(request);

    // then
    assertNotNull(result);
    verify(commentMapper).toCommentEntity(request);
    verify(commentRepository).save(mappedEntity);
    verify(commentMapper).toCommentDto(savedEntity);
  }

}
