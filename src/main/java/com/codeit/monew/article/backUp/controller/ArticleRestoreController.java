package com.codeit.monew.article.backUp.controller;

import com.codeit.monew.article.backUp.dto.ArticleRestoreResultDto;
import com.codeit.monew.article.backUp.service.ArticleRestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleRestoreController {
    private final ArticleRestoreService articleRestoreService;

    @GetMapping("/restore")
    public ResponseEntity<List<ArticleRestoreResultDto>> getArticleRestoreResults(
            @RequestParam("from") String from, // 시작지점
            @RequestParam("to") String to // 끝지점
    ) {
        List<ArticleRestoreResultDto> restore = articleRestoreService.restoreArticle(from, to);
        return ResponseEntity.ok(restore);
    }
}
