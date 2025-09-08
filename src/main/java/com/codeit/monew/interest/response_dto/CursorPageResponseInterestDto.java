package com.codeit.monew.interest.response_dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseInterestDto(
    List<InterestDto> content,
    String nextCursor,
    LocalDateTime nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {}
