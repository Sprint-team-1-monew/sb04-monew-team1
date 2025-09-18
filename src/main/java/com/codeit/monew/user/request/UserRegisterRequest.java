package com.codeit.monew.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserRegisterRequest(

    @NotBlank(message = "이메일 누락")
    @Email(message = " 이메일 형식이 올바르지 않음")
    @Length(min = 1, max = 320, message ="이메일 길이는 1자 이상 320자 이하여야 함")
    String email,

    @NotBlank(message = "닉네임 누락")
    @Length(min = 1, max = 20, message = "닉네임 길이는 1자 이상 20자 이하여야 함")
    String nickname,

    @NotBlank(message = "비밀번호 누락")
    @Length(min = 1, max = 80, message = "비밀번호 길이는 1자 이상 80자 이하여야 함")
    String password
) {

}
