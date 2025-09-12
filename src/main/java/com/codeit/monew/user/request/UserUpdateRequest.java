package com.codeit.monew.user.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserUpdateRequest(
    @NotNull(message = "닉네임 누락")
    @Length(min = 1, max = 20, message = "닉네임 길이는 1자 이상 20자 이하여야 함")
    String nickname
) {

}
