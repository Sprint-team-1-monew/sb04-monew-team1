package com.codeit.monew.interest.request;

import java.util.List;

public record InterestRegisterRequest(
  String name,
  List<String> keywords
) {}