package com.codeit.monew.user.request;

public record UserRegisterRequest(
    String email,
    String nickname,
    String password
) {}