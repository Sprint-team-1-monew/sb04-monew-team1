package com.codeit.monew.comment.repository.likeRepository;

import java.util.UUID;

public interface CommentLikeQuerydslRepositoryCustom {

  long deleteByCommentId(UUID commentId);

}
