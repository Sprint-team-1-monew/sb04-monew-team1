package com.codeit.monew.user.mapper;

import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.response_dto.UserDto;
import java.util.ArrayList;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "password", ignore = true) // 비밀번호 암호화는 서비스 계층에서 하기 위함
  @Mapping(target = "userStatus", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "subscriptions", ignore = true)
  @Mapping(target = "comments", ignore = true)
  @Mapping(target = "commentLikes", ignore = true)
  @Mapping(target = "articlesViewUsers", ignore = true)
  User toEntity(UserRegisterRequest userRegisterRequest);

  UserDto toDto(User user);

  @AfterMapping
  default void initUser(@MappingTarget User.UserBuilder user) {
    user.userStatus(UserStatus.ACTIVE);
    user.subscriptions(new ArrayList<>());
    user.comments(new ArrayList<>());
    user.commentLikes(new ArrayList<>());
    user.articlesViewUsers(new ArrayList<>());
  }

}
