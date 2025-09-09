package com.codeit.monew.comment.entity;

public enum CommentOrderBy {
  CREATED_AT("createdAt"),
  LIKE_COUNT("likeCount");

  private final String field;
  CommentOrderBy(String field) {
    this.field = field;
  }

  public String getField() {
    return field;
  }
}
