package com.codeit.monew.user.response_dto;

import java.time.LocalDateTime;

public record UserDto(
    String id,
    String email,
    String nickname,
    LocalDateTime createdAt
) {

}
