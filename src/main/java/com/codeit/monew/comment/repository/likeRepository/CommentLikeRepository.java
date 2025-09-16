package com.codeit.monew.comment.repository.likeRepository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID>, CommentLikeQuerydslRepositoryCustom{

  boolean existsByCommentIdAndUserId(UUID commentId, UUID requestUserId);

  Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
}
