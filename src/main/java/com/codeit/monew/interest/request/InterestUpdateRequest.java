package com.codeit.monew.interest.request;

import java.util.List;

public record InterestUpdateRequest(
  List<String> keywords
) {}