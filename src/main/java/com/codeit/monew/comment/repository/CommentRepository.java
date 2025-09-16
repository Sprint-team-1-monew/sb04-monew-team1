package com.codeit.monew.comment.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

  Optional<Comment> findById(UUID id);

  int countByArticle(Article article);
  Long countByArticleId(UUID id);
}
