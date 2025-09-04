package com.codeit.monew.interest.response_dto;

import java.util.List;

public record CursorPageResponseInterestDto(
  List<InterestDto> content,
  String nextCursor,
  java.time.Instant nextAfter,
  Integer size,
  Long totalElements,
  Boolean hasNext
) {}
