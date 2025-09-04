package com.codeit.monew.activity_management.response_dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionDto(
  UUID id,
  UUID interestId,
  String interestName,
  List<String> interestKeywords,
  Long interestSubscriberCount,
  LocalDateTime createdAt
) {}