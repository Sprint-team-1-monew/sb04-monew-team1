package com.codeit.monew.activity_management.response_dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserActivityDto(
  UUID id,
  String email,
  String nickname,
  LocalDateTime createdAt,
  List<UserArticleViewDto> articleViews,
  List<CommentActivityDto> comments,
  List<CommentLikeActivityDto> commentLikes,
  List<UserSubscriptionDto> subscriptions
) {

}
