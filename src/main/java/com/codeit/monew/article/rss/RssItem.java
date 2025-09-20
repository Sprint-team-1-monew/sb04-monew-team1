package com.codeit.monew.article.rss;

import java.time.LocalDateTime;

public record RssItem(
    String title,
    String link,
    LocalDateTime publishedAt
) {}
