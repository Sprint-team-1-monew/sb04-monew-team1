package com.codeit.monew.notification.controller;

import com.codeit.monew.notification.response_dto.CursorPageResponseNotificationDto;
import com.codeit.monew.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorPageResponseNotificationDto> getNotifications(@RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam int limit,
      @RequestHeader("Monew-Request-User-ID") UUID userId
      ) {
    CursorPageResponseNotificationDto response =
        notificationService.getUnconfirmedNotifications(userId, cursor, after, limit);
    return ResponseEntity.ok(response);
  }

  @PatchMapping
  public ResponseEntity<Void> patchAllNotifications(@RequestHeader("Monew-Request-User-ID") UUID userId
  ){
    notificationService.confirmAllNotifications(userId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{notificationId}")
  public ResponseEntity<Void> patchNotifications(@PathVariable UUID notificationId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
      ){
    notificationService.confirmNotification(notificationId, userId);
    return ResponseEntity.ok().build();
  }
}
