package com.codeit.monew.activity_management.controller;

import com.codeit.monew.activity_management.response_dto.UserActivityDto;
import com.codeit.monew.activity_management.service.UserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-activities")
public class UserActivityController {
  private final UserActivityService userActivityService;

  @GetMapping("/{userId}")
  ResponseEntity<UserActivityDto> getUserActivity(@PathVariable UUID userId) {
    return ResponseEntity.ok(userActivityService.getUserActivity(userId));
  }
}
