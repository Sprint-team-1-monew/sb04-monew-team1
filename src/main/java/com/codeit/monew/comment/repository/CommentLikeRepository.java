package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

  boolean existsByCommentIdAndUserId(UUID commentId, UUID requestUserId);

  Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
}
