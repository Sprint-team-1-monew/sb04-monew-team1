package com.codeit.monew.notification.response_dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto (
  UUID id,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  boolean confirmed,
  UUID userId,
  String content,
  String resourceType,
  UUID resourceId
){}
