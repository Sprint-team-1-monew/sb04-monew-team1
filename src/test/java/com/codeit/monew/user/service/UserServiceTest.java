package com.codeit.monew.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.mapper.UserMapper;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.request.UserUpdateRequest;
import com.codeit.monew.user.response_dto.UserDto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
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

  private static User createUser() {
    String password = "password";
    return User.builder()
        .email("email@email.com")
        .nickname("nickname")
        .password(BCrypt.withDefaults().hashToString(12, password.toCharArray()))
        .userStatus(UserStatus.ACTIVE)
        .build();
  }

  private static UserDto createUserDto(UUID userId) {
    return new UserDto(userId.toString(), "email@email.com", "nickname", LocalDateTime.now());
  }

}
