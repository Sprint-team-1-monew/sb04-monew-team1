package com.codeit.monew.interest.entity;

import com.codeit.monew.base.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "interests")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Interest extends BaseUpdatableEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "subscriber_count", nullable = false)
  private Integer subscriberCount;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  public void increaseSubscriber() {
    this.subscriberCount += 1;
  }

  public void decreaseSubscriber() {
    if (this.subscriberCount == 0) {
      throw new IllegalStateException("구독자 수는 0보다 작아질 수 없습니다.");
    }
    this.subscriberCount -= 1;
  }
}