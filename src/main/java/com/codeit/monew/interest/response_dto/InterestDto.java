package com.codeit.monew.interest.response_dto;

import java.util.List;
import java.util.UUID;

public record InterestDto(
  UUID id,
  String name,
  List<String> keywords,
  Long subscriberCount,
  Boolean subscribedByMe
) {}