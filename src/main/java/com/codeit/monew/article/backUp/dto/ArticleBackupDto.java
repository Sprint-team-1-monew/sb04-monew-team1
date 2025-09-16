package com.codeit.monew.article.backUp.dto;

import com.codeit.monew.article.entity.Article;

import java.time.LocalDateTime;

public record ArticleBackupDto(
        String source,
        String sourceUrl,
        String articleTitle,
        LocalDateTime articlePublishDate,
        String articleSummary,
        long articleCommentCount,
        long articleViewCount,
        boolean deleted,
        String interestId // 복구할 때 필요함, 관심사 ID를 가져옴
) {
    public static ArticleBackupDto from(Article article) {
        String interestIdStr = (article.getInterest() != null && article.getInterest().getId() != null) // Interest가 Lazy해도 ID접근을 프록시로 가능하게끔
                ? article.getInterest().getId().toString()
                : null;

        return new ArticleBackupDto(
                article.getSource(),
                article.getSourceUrl(),
                article.getArticleTitle(),
                article.getArticlePublishDate(),
                article.getArticleSummary(),
                article.getArticleCommentCount(),
                article.getArticleViewCount(),
                article.isDeleted(),
                interestIdStr
        );
    }
}
