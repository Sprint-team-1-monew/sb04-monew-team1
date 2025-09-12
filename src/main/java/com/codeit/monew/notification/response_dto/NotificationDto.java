package com.codeit.monew.notification.response_dto;

import com.codeit.monew.notification.entity.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto (
  UUID id,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  boolean confirmed,
  UUID userId,
  String content,
  ResourceType resourceType,
  UUID resourceId
){}
