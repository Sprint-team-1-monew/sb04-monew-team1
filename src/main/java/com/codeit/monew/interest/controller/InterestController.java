package com.codeit.monew.interest.controller;

import com.codeit.monew.interest.request.InterestRegisterRequest;
import com.codeit.monew.interest.response_dto.InterestDto;
import com.codeit.monew.interest.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

  private final InterestService interestService;

  @PostMapping
  public ResponseEntity<InterestDto> registerInterest(@RequestBody InterestRegisterRequest request) {
    InterestDto result = interestService.registerInterest(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }
}