package com.codeit.monew.interest.mapper;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.response_dto.InterestDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterestMapper {

  @Mapping(target = "keywords", expression = "java(keywords.stream().map(k -> k.getKeyword()).toList())")
  InterestDto toDto(Interest interest, List<Keyword> keywords, Boolean subscribedByMe);
}