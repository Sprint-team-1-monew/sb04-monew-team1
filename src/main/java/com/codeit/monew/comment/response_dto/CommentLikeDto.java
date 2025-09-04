package com.codeit.monew.comment.response_dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeDto (
  UUID id,
  UUID likedBy,
  LocalDateTime createdAt,
  UUID commentId,
  UUID articleId,
  UUID commentUserId,
  String commentUserNickname,
  String commentContent,
  Integer commentLikeCount,
  LocalDateTime commentCreatedAt
) {}