package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {

  List<Comment> findComments(UUID articleId,
      CommentOrderBy orderBy,
      SortDirection direction,
      String cursor,
      LocalDateTime after,
      int limit);

}
