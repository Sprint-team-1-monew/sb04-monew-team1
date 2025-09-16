package com.codeit.monew.user.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.mapper.UserMapper;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.request.UserLoginRequest;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.request.UserUpdateRequest;
import com.codeit.monew.user.response_dto.UserDto;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자 {}", userId);
          return new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
        });

    Optional.ofNullable(userUpdateRequest.nickname()).ifPresent(user::updateNickname);
    return userMapper.toDto(userRepository.save(user));
  }

  public UserDto login(UserLoginRequest userLoginRequest) {
    User user = userRepository.findByEmail(userLoginRequest.email())
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자 {}", userLoginRequest.email());
          return new UserException(UserErrorCode.USER_LOGIN_FAILED, Map.of("email", userLoginRequest.email()));
        });

    String loginPassword = userLoginRequest.password();

    if(!matches(loginPassword, user.getPassword())) {
      throw new UserException(UserErrorCode.USER_LOGIN_FAILED, Map.of("auth", "failed"));
    }

    return userMapper.toDto(user);
  }

  private void validateEmailDoesNotExist(String email) {
    if (userRepository.existsByEmail(email)) {
      log.warn("중복된 이메일 {}", email);
      throw new UserException(UserErrorCode.USER_EMAIL_DUPLICATED, Map.of("email", email));
    }
  }

  private boolean matches(String plainPassword, String hashedPassword) {
    return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified;
  }

  public void softDelete(UUID userId) {
    User softDeletedUser = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자 {}", userId);
          return new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
        });

    if(isUserStatusDeleted(softDeletedUser.getUserStatus())) {  // 이미 논리 삭제 된 경우 논리 삭제 시간 갱신 방지
      return;
    }

    softDeletedUser.updateUserStatus(UserStatus.DELETED);
    softDeletedUser.updateDeletedAt(LocalDateTime.now());

    userRepository.save(softDeletedUser);
  }

  private boolean isUserStatusDeleted(UserStatus userStatus) {
    return userStatus.equals(UserStatus.DELETED);
  }

  public void hardDelete(UUID userId) {
    User hardDeletedUser = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자 {}", userId);
          return new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
        });

    if(!isUserStatusDeleted(hardDeletedUser.getUserStatus())) {
      throw new UserException(UserErrorCode.USER_NOT_DELETABLE, Map.of("userId", userId));
    }

    if(isSoftDeletedBeforeFiveMinutes(hardDeletedUser.getDeletedAt())) {
      userRepository.delete(hardDeletedUser);
    }
  }

  //프로토 타입용으로 5분으로 설정
  private boolean isSoftDeletedBeforeFiveMinutes(LocalDateTime deletedAt) {
    return deletedAt.isBefore(LocalDateTime.now().minusMinutes(5)) ||
        deletedAt.isEqual(LocalDateTime.now().minusMinutes(5));
  }
}
