package com.codeit.monew.comment.mapper;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.user.entity.User;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(target = "user", expression = "java(mapUser(commentRegisterRequest.userId()))")
  @Mapping(target = "article", expression = "java(mapArticle(commentRegisterRequest.articleId()))")
  Comment toCommentEntity(CommentRegisterRequest commentRegisterRequest);

  Comment toCommentEntity(CommentUpdateRequest commentUpdateRequest);

  CommentDto toCommentDto(Comment comment);


  // User 매핑용 추가
  default User mapUser(UUID userId) {
    if (userId == null) {
      return null;
    }
    return User.builder()
        .id(userId)
        .build();
  }

  // Article 매핑용 추가
  default Article mapArticle(UUID articleId) {
    if (articleId == null) {
      return null;
    }
    return Article.builder()
        .id(articleId)
        .build();
  }
}
