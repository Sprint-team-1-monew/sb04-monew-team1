package com.codeit.monew.interest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InterestUpdateRequest(
    @NotNull(message = "키워드 목록은 필수입니다")
    @NotEmpty(message = "키워드는 최소 1개 이상 입력해야 합니다")
    @Size(min = 1, max = 10, message = "키워드는 1개 이상 10개 이하여야 합니다")
    List<@NotBlank(message = "키워드는 비어있을 수 없습니다") String> keywords
) {}
