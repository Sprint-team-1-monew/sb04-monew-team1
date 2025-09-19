package com.codeit.monew.article.mapper;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.response_dto.ArticleViewDto;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // 스프링 빈으로 등록
public interface ArticleViewMapper {

  @Mapping(target = "id",                 source = "article.id")
  @Mapping(target = "viewedBy",           source = "requestUserId")
  @Mapping(target = "createdAt",          source = "viewCreatedAt")
  @Mapping(target = "source",             source = "article.source")
  @Mapping(target = "sourceUrl",          source = "article.sourceUrl")
  @Mapping(target = "articleTitle",       source = "article.articleTitle")
  @Mapping(target = "articlePublishDate", source = "article.articlePublishDate")
  @Mapping(target = "articleSummary",     source = "article.articleSummary")
  @Mapping(target = "articleCommentCount",source = "article.articleCommentCount")
  @Mapping(target = "articleViewCount",   source = "article.articleViewCount")
  ArticleViewDto toDto(Article article, UUID requestUserId, LocalDateTime viewCreatedAt);
}
