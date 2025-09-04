package com.codeit.monew.user.entity;

import com.codeit.monew.article.entity.ArticlesViewUser;
import com.codeit.monew.base.entity.BaseUpdatableEntity;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.subscriptions.entity.Subscription;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
public class User extends BaseUpdatableEntity {

  @Column(name = "email", nullable = false, length = 30, unique = true)
  private String email;

  @Column(name = "nickname", nullable = false, length = 20)
  private String nickname;

  @Column(name = "password", nullable = false, length = 80)
  private String password;

  //사용자 논리삭제 여부 상황에 따라 추후 사용자 상태 추가 가능하다고 생각
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private UserStatus userStatus;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "deleted_at", nullable = true)
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<Subscription> subscriptions;

  @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<Comment> comments;

  @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<CommentLike> commentLikes;

  @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<ArticlesViewUser> articlesViewUsers;

  public void updateEmail(String email) {
    this.email = email;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

  public void updateUserStatus(UserStatus userStatus) {
    this.userStatus = userStatus;
  }

  public void updateDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }
}
