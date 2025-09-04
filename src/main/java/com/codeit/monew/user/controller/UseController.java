package com.codeit.monew.user.controller;

import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.response_dto.UserDto;
import com.codeit.monew.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UseController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> registerUser(@Valid UserRegisterRequest userRegisterRequest) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRegisterRequest));
  }
}
