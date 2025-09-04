package com.codeit.monew.user.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserRegisterRequest(

    @NotNull(message = "이메일 누락")
    @Length(min = 1, max = 320, message ="이메일 길이는 1자 이상 320자 이하여야 함")
    String email,

    @NotNull(message = "닉네임 누락")
    @Length(min = 1, max = 20, message = "닉네임 길이는 1자 이상 20자 이하여야 함")
    String nickname,

    @NotNull(message = "비밀번호 누락")
    @Length(min = 1, max = 80, message = "비밀번호 길이는 1자 이상 80자 이하여야 함")
    String password
) {

}
