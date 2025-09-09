package com.codeit.monew.comment.response_dto;

import com.codeit.monew.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CommentListResponse {

  private UUID id;
  private UUID articleId;
  private String content;
  private UUID userId;
  private LocalDateTime createdAt;

  public static CommentListResponse fromEntity(Comment comment) {
    return CommentListResponse.builder()
        .id(comment.getId())
        .articleId(comment.getArticle().getId())
        .content(comment.getContent())
        .userId(comment.getUser().getId())
        .createdAt(comment.getCreatedAt())
        .build();
  }
}
