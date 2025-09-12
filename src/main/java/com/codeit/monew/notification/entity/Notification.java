package com.codeit.monew.notification.entity;

import com.codeit.monew.base.entity.BaseUpdatableEntity;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
public class Notification extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Setter
  @Column(name = "confirmed", nullable = false)
  private boolean confirmed;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Column(name = "resource_type", nullable = false, length = 10)
  @Enumerated(EnumType.STRING)
  private ResourceType resourceType;

  @Column(name = "resource_id", nullable = false)
  private UUID resourceId;
}
