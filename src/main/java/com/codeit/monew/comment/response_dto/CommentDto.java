package com.codeit.monew.comment.response_dto;

import com.codeit.monew.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto (
    UUID commentId,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    Long likeCount,
    Boolean likedByMe,
    LocalDateTime createdAt
){
  public static CommentDto fromEntity(Comment comment){
    return new CommentDto(
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        (long) comment.getLikeCount(),
        false, // likedByMe는 기본값, 필요 시 서비스에서 계산
        comment.getCreatedAt()
        );
  }
}
