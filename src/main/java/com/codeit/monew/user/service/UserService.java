package com.codeit.monew.user.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.mapper.UserMapper;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.request.UserUpdateRequest;
import com.codeit.monew.user.response_dto.UserDto;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final UserMapper userMapper;

  public UserDto registerUser(UserRegisterRequest userRegisterRequest) {
    validateEmailDoesNotExist(userRegisterRequest.email());

    User newUser = userMapper.toEntity(userRegisterRequest);
    String plainPassword = userRegisterRequest.password();
    String hashedPassword = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());

    newUser.updatePassword(hashedPassword);
    return userMapper.toDto(userRepository.save(newUser));
  }

  public UserDto updateUser(UUID userId, UserUpdateRequest userUpdateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));

    Optional.ofNullable(userUpdateRequest.nickname()).ifPresent(user::updateNickname);
    return userMapper.toDto(userRepository.save(user));
  }

  private void validateEmailDoesNotExist(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new UserException(UserErrorCode.USER_EMAIL_DUPLICATED, Map.of("email", email));
    }
  }

}
