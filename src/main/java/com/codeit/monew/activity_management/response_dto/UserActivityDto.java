package com.codeit.monew.activity_management.response_dto;

import com.codeit.monew.article.response_dto.ArticleViewDto;
import com.codeit.monew.interest.response_dto.SubscriptionDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserActivityDto(
  UUID id,
  String email,
  String nickname,
  LocalDateTime createdAt,
  List<SubscriptionDto> subscriptions,
  List<CommentActivityDto> comments,
  List<CommentLikeActivityDto> commentLikes,
  List<ArticleViewDto> articleViews
) {

}
