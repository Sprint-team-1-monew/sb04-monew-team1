package com.codeit.monew.interest.response_dto;

import java.util.List;
import java.util.UUID;

public record SubscriptionDto(
  UUID id,
  UUID interestId,
  String interestName,
  List<String> interestKeywords,
  Long interestSubscriberCount,
  java.time.Instant createdAt
) {}