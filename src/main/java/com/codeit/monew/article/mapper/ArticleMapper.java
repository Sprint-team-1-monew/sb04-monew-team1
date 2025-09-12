package com.codeit.monew.article.mapper;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.response_dto.ArticleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // 스프링 빈으로 등록
public interface ArticleMapper {

  @Mapping(source = "article.id", target = "id")
  @Mapping(source = "article.source", target = "source")
  @Mapping(source = "article.sourceUrl", target = "sourceUrl")
  @Mapping(source = "article.articleTitle", target = "title")
  @Mapping(source = "article.articlePublishDate", target = "publishDate")
  @Mapping(source = "article.articleSummary", target = "summary")
  @Mapping(source = "article.articleCommentCount", target = "commentCount")
  @Mapping(source = "article.articleViewCount", target = "viewCount")
  @Mapping(source = "viewedByMe", target = "viewedByMe") // 외부 파라미터 매핑
  ArticleDto toDto(Article article, boolean viewedByMe);
}