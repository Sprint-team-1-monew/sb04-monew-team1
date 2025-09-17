package com.codeit.monew.comment.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentRegisterRequest(

    @NotNull(message = "기사 게시글 ID는 필수입니다.")
    UUID articleId,

    @NotNull(message = "사용자 ID 는 필수입니다.")
    UUID userId,

    @NotNull(message = "댓글 내용은 필수입니다.")
    String content
) {

}
