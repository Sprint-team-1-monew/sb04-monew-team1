package com.codeit.monew.comment.mapper;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  Comment toCommentEntity(CommentRegisterRequest commentRegisterRequest);

  CommentDto toCommentDto(Comment comment);
}
