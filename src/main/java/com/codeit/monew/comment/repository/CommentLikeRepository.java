package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.CommentLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {



}
