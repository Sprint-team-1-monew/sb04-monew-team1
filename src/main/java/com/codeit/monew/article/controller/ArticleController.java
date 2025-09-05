package com.codeit.monew.article.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

  @DeleteMapping("/{articleId}")
  public ResponseEntity<?> deleteArticle(@PathVariable("articleId") String articleId) {


    return ResponseEntity.noContent().build();
  }

}
