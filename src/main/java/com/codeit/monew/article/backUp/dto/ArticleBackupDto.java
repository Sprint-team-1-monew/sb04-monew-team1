package com.codeit.monew.article.backUp.dto;

import com.codeit.monew.article.entity.Article;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleBackupDto(
        UUID id,
        LocalDateTime createdAt,
        String source,
        String sourceUrl,
        String articleTitle,
        LocalDateTime articlePublishDate,
        String articleSummary,
        long articleCommentCount,
        long articleViewCount,
        boolean deleted,
        UUID interestId // 복구할 때 필요함, 관심사 ID를 가져옴
) {
    public static ArticleBackupDto from(Article article) {
        return new ArticleBackupDto(
                article.getId(),
                article.getCreatedAt(),
                article.getSource(),
                article.getSourceUrl(),
                article.getArticleTitle(),
                article.getArticlePublishDate(),
                article.getArticleSummary(),
                article.getArticleCommentCount(),
                article.getArticleViewCount(),
                article.isDeleted(),
                article.getInterest().getId() // null일 수 있음
        );
    }
}
