package com.codeit.monew.comment.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

  Optional<Comment> findById(UUID id);

  Long countByArticleId(UUID id);

  List<Comment> findByArticle(Article checkedArticle);
}
