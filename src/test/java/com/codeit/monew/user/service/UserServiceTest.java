package com.codeit.monew.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.mapper.UserMapper;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.request.UserLoginRequest;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.request.UserUpdateRequest;
import com.codeit.monew.user.response_dto.UserDto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserService userService;

  private static UUID userId;

  private static User user;

  private static UserDto userDto;

  @BeforeAll
  static void setUp() {
    userId = UUID.randomUUID();
    user = createUser();
    ReflectionTestUtils.setField(user, "id", userId);
    userDto = createUserDto(userId);
  }


  @Test
  @DisplayName("사용자를 등록에 성공한다")
  void registerUser_Success() {
    //given
    UserRegisterRequest userRegisterRequest = new UserRegisterRequest("email@email.com", "nickname", "password");
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toEntity(userRegisterRequest)).willReturn(user);
    given(userMapper.toDto(user)).willReturn(userDto);

    //when
    UserDto userResponse = userService.registerUser(userRegisterRequest);

    //then
    assertThat(userResponse).isEqualTo(userDto);
    then(userRepository).should(times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("등록하려는 사용자의 이메일이 이미 가입되어 있으면 테스트에 실패한다")
  void registerUser_Fail_WhenEmailExists() {
    //given
    UserRegisterRequest userRegisterRequest = new UserRegisterRequest("email@email.com", "nickname", "password");
    given(userRepository.existsByEmail(userRegisterRequest.email())).willReturn(true);

    //when & then
    assertThatThrownBy(() -> userService.registerUser(userRegisterRequest)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("사용자 닉네임 수정에 성공한다")
  void updateUserNickname_Success() {
    //given
    UserUpdateRequest userUpdateRequest = new UserUpdateRequest("updateNickname");
    UserDto updateResponse = new UserDto(userId.toString(), "email@email.com", "updateNickname", LocalDateTime.now());
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(user)).willReturn(updateResponse);

    //when
    UserDto userResponse = userService.updateUser(userId, userUpdateRequest);

    //then
    assertThat(userResponse).isEqualTo(updateResponse);
    then(userRepository).should(times(1)).findById(any(UUID.class));
    then(userRepository).should(times(1)).save(any(User.class));

  }

  @Test
  @DisplayName("존재하지 않는 사용자 수정 시 실패한다")
  void updateUserNickname_Fail_WhenUserNotFound() {
    //given
    UserUpdateRequest userUpdateRequest = new UserUpdateRequest("updateNickname");
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() -> userService.updateUser(userId, userUpdateRequest)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("사용자 로그인에 성공한다")
  void login_Success() {
    //given
    UserLoginRequest userLoginRequest = new UserLoginRequest("email@email.com", "password");
    UserDto userDto = new UserDto(userId.toString(), "email@email.com", "nickname",
        LocalDateTime.now());
    given(userRepository.findByEmail(any(String.class))).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(userDto);

    //when
    UserDto userResponse = userService.login(userLoginRequest);

    //then
    assertThat(userResponse).isEqualTo(userDto);
    then(userRepository).should(times(1)).findByEmail(any(String.class));
    then(userMapper).should(times(1)).toDto(any(User.class));
  }

  @Test
  @DisplayName("가입되지 않은 이메일로 로그인 시도 시 로그인에 실패한다")
  void login_Fail_WhenEmailNotExists() {
    //given
    UserLoginRequest userLoginRequest = new UserLoginRequest("email@email.com", "password");
    given(userRepository.findByEmail(any(String.class))).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() -> userService.login(userLoginRequest)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
  void login_Fail_WhenPasswordNotMatch() {
    //given
    UserLoginRequest userLoginRequest = new UserLoginRequest("email@email.com", "password!");
    given(userRepository.findByEmail(any(String.class))).willReturn(Optional.of(user));

    //when & then
    assertThatThrownBy(() -> userService.login(userLoginRequest)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("사용자 논리삭제에 성공한다")
  void userSoftDelete_Success() {
    //given
    User softDeletedUser = User.builder()
        .email("email@email.com")
        .nickname("test")
        .password(BCrypt.withDefaults().hashToString(12, "password".toCharArray()))
        .userStatus(UserStatus.DELETED)
        .build();

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
    given(userRepository.save(any(User.class))).willReturn(softDeletedUser);

    //when
    userService.softDelete(userId);

    //then
    then(userRepository).should(times(1)).findById(any(UUID.class));
    then(userRepository).should(times(1)).save(any(User.class));

  }

  @Test
  @DisplayName("존재하지 않는 사용자 논리삭제 시도 시 실패")
  void userSoftDelete_Fail_WhenUserNotFound() {
    //given
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() -> userService.softDelete(userId)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("논리삭제한 사용자 논리삭제 재시도 시 삭제된 시간이 갱신되지 않는다")
  void userSoftDelete_Not_Update_DeletedAt_WhenUserAlreadyDeleted() {
    //given
    User softDeletedUser = createUser();
    ReflectionTestUtils.setField(softDeletedUser, "userStatus", UserStatus.DELETED);
    ReflectionTestUtils.setField(softDeletedUser, "deletedAt", LocalDateTime.now().minusMinutes(3));
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(softDeletedUser));

    //when
    userService.softDelete(userId);

    //then
    Assertions.assertThat(softDeletedUser.getDeletedAt()).isNotEqualTo(LocalDateTime.now());
  }

  @Test
  @DisplayName("사용자 물리 삭제에 성공한다")
  void deleteHardDelete_Success() {
    //given
    User softDeletedUser = User.builder()
        .email("email@email.com")
        .nickname("test")
        .password(BCrypt.withDefaults().hashToString(12, "password".toCharArray()))
        .userStatus(UserStatus.DELETED)
        .deletedAt(LocalDateTime.now().minusMinutes(5))
        .build();

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(softDeletedUser));

    //when
    userService.hardDelete(userId);

    //then
    then(userRepository).should(times(1)).findById(any(UUID.class));
    then(userRepository).should(times(1)).delete(eq(softDeletedUser));

  }

  @Test
  @DisplayName("존재하지 않는 사용자 물리 삭제 시 실패")
  void deleteHardDelete_Fail_WhenUserNotFound() {
    //given
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() -> userService.hardDelete(userId)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("논리삭제 상태가 아닌 사용자 물리삭제 시도 시 실패")
  void deleteHardDelete_Fail_WhenUserNotDeleted() {
    //given
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));

    //when & then
    assertThatThrownBy(() -> userService.hardDelete(userId)).isInstanceOf(
        UserException.class);
  }

  @Test
  @DisplayName("논리삭제된지 5분 미만이면 물리삭제되지 않는다")
  void deleteHardDelete_Not_Delete_BeforeFiveMinutes() {
    //given
    User softDeletedUser = createUser();
    ReflectionTestUtils.setField(softDeletedUser, "userStatus", UserStatus.DELETED);
    ReflectionTestUtils.setField(softDeletedUser, "deletedAt", LocalDateTime.now().minusMinutes(3));
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(softDeletedUser));

    //when
    userService.hardDelete(userId);

    //then
    then(userRepository).should(times(0)).delete(any(User.class));
  }

  @Test
  @DisplayName("논리삭제된지 5분 이상이면 물리삭제된다")
  void deleteHardDelete_Delete_AfterFiveMinutes() {
    //given
    User softDeletedUser = createUser();
    ReflectionTestUtils.setField(softDeletedUser, "userStatus", UserStatus.DELETED);
    ReflectionTestUtils.setField(softDeletedUser, "deletedAt", LocalDateTime.now().minusMinutes(5));
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(softDeletedUser));

    //when
    userService.hardDelete(userId);

    //then
    then(userRepository).should(times(1)).delete(any(User.class));
  }

  private static User createUser() {
    return User.builder()
        .email("email@email.com")
        .nickname("nickname")
        .password("password")
        .userStatus(UserStatus.ACTIVE)
        .build();
  }

  private static UserDto createUserDto(UUID userId) {
    return new UserDto(userId.toString(), "email@email.com", "nickname", LocalDateTime.now());
  }

}
