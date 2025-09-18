package com.codeit.monew.article.backUp.service;

import com.codeit.monew.article.backUp.dto.ArticleRestoreResultDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ArticleRestoreService {
    ArticleRestoreResultDto restoreArticle(String from, String to);
}
