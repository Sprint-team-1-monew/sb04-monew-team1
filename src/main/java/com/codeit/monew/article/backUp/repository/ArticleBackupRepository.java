package com.codeit.monew.article.backUp.repository;

import com.codeit.monew.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ArticleBackupRepository extends JpaRepository<Article, UUID> {
    @Query("""
        select art from Article as art
        where art.articlePublishDate >= :startDate and art.articlePublishDate < :endDate

    """)
    Page<Article> findAllInRange(
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate")LocalDateTime endDate,
            Pageable pageable
            );

    @Query("""
        select art.sourceUrl from Article as art
        where art.articlePublishDate >= :startDate and art.articlePublishDate < :endDate
    """)
    List<String> findAllSourceUrlsInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
