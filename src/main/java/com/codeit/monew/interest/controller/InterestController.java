package com.codeit.monew.interest.controller;

import com.codeit.monew.interest.request.InterestRegisterRequest;
import com.codeit.monew.interest.response_dto.CursorPageResponseInterestDto;
import com.codeit.monew.interest.response_dto.InterestDto;
import com.codeit.monew.interest.service.InterestService;
import com.codeit.monew.subscriptions.dto.SubscriptionDto;
import com.codeit.monew.subscriptions.service.SubscriptionService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

  private final InterestService interestService;
  private final SubscriptionService subscriptionService;

  @PostMapping
  public ResponseEntity<InterestDto> registerInterest(
      @RequestBody InterestRegisterRequest request) {
    InterestDto result = interestService.registerInterest(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseInterestDto> getInterests(
      @RequestParam(required = false) String keyword,
      @RequestParam String orderBy,
      @RequestParam String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId
  ) {
    CursorPageResponseInterestDto response = interestService.searchInterests(
        keyword, orderBy, direction, cursor, after, limit, requestUserId
    );
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{interestId}/subscriptions")
  public ResponseEntity<SubscriptionDto> subscribeInterest(
      @PathVariable UUID interestId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    SubscriptionDto dto = subscriptionService.subscribe(userId, interestId);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }
}