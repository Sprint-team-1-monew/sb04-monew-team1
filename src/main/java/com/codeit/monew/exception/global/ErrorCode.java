package com.codeit.monew.exception.global;

public interface ErrorCode {

  int getStatus();

  String getMessage();

  String getName();
}