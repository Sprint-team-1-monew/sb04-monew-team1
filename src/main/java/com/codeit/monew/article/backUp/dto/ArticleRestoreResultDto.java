package com.codeit.monew.article.backUp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRestoreResultDto(
        LocalDateTime restoreDate, // String
        List<UUID> restoredArticleIds,
        long restoredArticleCount // int64
) {
}
