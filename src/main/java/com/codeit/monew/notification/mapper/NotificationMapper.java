package com.codeit.monew.notification.mapper;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.response_dto.NotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  @Mapping(target = "userId", source = "user.id")
  NotificationDto toDto(Notification notification);
}
