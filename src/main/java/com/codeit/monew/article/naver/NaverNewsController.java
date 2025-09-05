package com.codeit.monew.article.naver;

import com.codeit.monew.article.service.NaverNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@RestController
public class NaverNewsController {
  private final NaverNewsService naverNewsService;

  @GetMapping("/news/search")
  public NaverNewsResponse search(@RequestParam String keyword
      , @RequestParam(defaultValue = "10") Integer display
      , @RequestParam(defaultValue = "1") Integer start
      , @RequestParam(defaultValue = "date") String sort) {
    return naverNewsService.searchNews(keyword, display, start, sort);
  }
  // 코드가 잘 작동되는지 확인 용 나중에 지우기
}
