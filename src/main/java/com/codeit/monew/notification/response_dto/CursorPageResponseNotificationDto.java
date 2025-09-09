package com.codeit.monew.notification.response_dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseNotificationDto(
  List<NotificationDto> content,
  String nextCursor,
  LocalDateTime nextAfter,
  Integer size,
  Long totalElements,
  Boolean hasNext
) {}
