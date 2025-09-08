package com.codeit.monew.article.naver;

public record NaverNewsItem(
    String title,
    String originallink,
    String link,
    String description,
    String pubDate
) {}