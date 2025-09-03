package com.codeit.monew.comment.response_dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto (
    UUID commentId,
    UUID articleId,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    Boolean likedByMe,
    LocalDateTime createdAt
){}