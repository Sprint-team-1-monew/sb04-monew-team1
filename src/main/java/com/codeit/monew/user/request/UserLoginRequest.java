package com.codeit.monew.user.request;

public record UserLoginRequest(
  String email,
  String password
) {}