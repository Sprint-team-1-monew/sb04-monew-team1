package com.codeit.monew.comment.mapper;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(target = "user.id", source = "userId")
  @Mapping(target = "article.id", source = "articleId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "likeCount", ignore = true)
  Comment toCommentEntity(CommentRegisterRequest commentRegisterRequest);

  @Mapping(target = "articleId",     source = "comment.article.id")
  @Mapping(target = "userId",        source = "comment.user.id")
  @Mapping(target = "userNickname",  source = "comment.user.nickname")
  @Mapping(target = "likedByMe",     source = "likedByMe")
  CommentDto toCommentDto(Comment comment, boolean likedByMe);
  }

