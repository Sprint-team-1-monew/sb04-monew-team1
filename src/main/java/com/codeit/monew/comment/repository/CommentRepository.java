package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<Comment, UUID> {

  Optional<Comment> findById(UUID id);
}
