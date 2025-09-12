package com.codeit.monew.user.controller;

import com.codeit.monew.user.request.UserLoginRequest;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.request.UserUpdateRequest;
import com.codeit.monew.user.response_dto.UserDto;
import com.codeit.monew.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRegisterRequest));
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
    return ResponseEntity.ok(userService.updateUser(userId, userUpdateRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
    return ResponseEntity.ok(userService.login(userLoginRequest));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> softDeleteUser(@PathVariable UUID userId) {
    userService.softDelete(userId);
    return ResponseEntity.noContent().build();
  }

  // 클라이언트에 노출x
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> shardDeleteUser(@PathVariable UUID userId) {
    userService.hardDelete(userId);
    return ResponseEntity.noContent().build();
  }
}
