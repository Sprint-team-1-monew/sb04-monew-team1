package com.codeit.monew.user.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.request.UserLoginRequest;
import com.codeit.monew.user.request.UserRegisterRequest;
import com.codeit.monew.user.request.UserUpdateRequest;
import com.codeit.monew.user.response_dto.UserDto;
import com.codeit.monew.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {UserController.class})
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  private static UserRegisterRequest userRegisterRequest;
  private static UserUpdateRequest userUpdateRequest;
  private static UserLoginRequest userLoginRequest;

  private static User user;
  private static UUID userId;

  private static UserDto userDto;

  @BeforeAll
  static void setup() {
    userRegisterRequest = new UserRegisterRequest("test@test.com","test", "test");
    userUpdateRequest = new UserUpdateRequest("updatedNickname");
    userLoginRequest = new UserLoginRequest("test@test.com", "test");

    userId = UUID.randomUUID();

    user = createUser();
    ReflectionTestUtils.setField(user, "id", userId);
    userDto = new UserDto(userId.toString(), "test@test.com", "test", user.getCreatedAt());
  }

  @Test
  @DisplayName("사용자 등록에 성공한다")
  void registerUser_Success() throws Exception {
    //given
    given(userService.registerUser(eq(userRegisterRequest))).willReturn(userDto);

    //when & then
    mockMvc.perform(
        post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRegisterRequest))
    )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.nickname").value("test"));

  }

  @Test
  @DisplayName("유효하지 않은 입력값으로 사용자 등록 시도 시 실패한다")
  void registerUser_Fail_WhenInvalidInput() throws Exception {
    //given
    UserRegisterRequest invalidRegisterRequest = new UserRegisterRequest(
        "",          // 이메일 형식, 1자 이상 320자 이하여야 함. 빈문자, 공백문자로만 이루어지면 안됨
        "",          // 닉네임 1자 이상 20자 이하여야 함. 빈문자, 공백문자로만 이루어지면 안됨
        "");         // 비밀번호는 1자 이상 80자 이하여야 함. 빈문자, 공백문자로만 이루어지면 안됨

    //when & then
    mockMvc.perform(
        post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRegisterRequest))
    )
        .andExpect(status().isBadRequest())
        .andExpect(result ->
            assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

  }

  @Test
  @DisplayName("사용자 닉네임 수정에 성공한다")
  void updateUserNickName_Success() throws Exception {
    //given
    UserDto updateResponse = new UserDto(userId.toString(), "test@test.com", "updatedNickname", user.getCreatedAt());
    given(userService.updateUser(any(UUID.class), eq(userUpdateRequest))).willReturn(updateResponse);

    //when & then
    mockMvc.perform(
        patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest))
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.nickname").value("updatedNickname"));

  }

  @Test
  @DisplayName("유효하지 않은 입력값으로 닉네임 수정 시도 시 실패한다")
  void updateUserNickName_Failed_WhenInvalidInput() throws Exception {
    //given
    UserUpdateRequest invalidUpdateRequest = new UserUpdateRequest(""); // 닉네임 1자 이상 20자 이하여야 함. 빈문자, 공백문자로만 이루어지면 안됨

    //when & then
    mockMvc.perform(
        patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidUpdateRequest))
    )
        .andExpect(status().isBadRequest())
        .andExpect(result ->
            assertInstanceOf(MethodArgumentNotValidException.class,
                result.getResolvedException()));

  }

  @Test
  @DisplayName("사용자 로그인 성공 테스트")
  void login_Success() throws Exception {
    //given
    given(userService.login(eq(userLoginRequest))).willReturn(userDto);

    //when & then
    mockMvc.perform(
        post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginRequest))
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.nickname").value("test"));

  }

  @Test
  @DisplayName("유효하지 않은 입력값으로 로그인 시도 시 실패하는 테스트")
  void login_Failed_WhenInvalidInput() throws Exception {
    //given
    UserLoginRequest invalidLoginRequest = new UserLoginRequest(
        "",          // 이메일 형식, 1자 이상 320자 이하여야함. 빈문자, 공백문자로만 이루어지면 안됨
        "");         // 비밀번호는 1자 이상 80자 이하여야함. 빈문자, 공백문자로만 이루어지면 안됨

    //when & then
    mockMvc.perform(
        post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidLoginRequest))
    )
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class
        , result.getResolvedException()));

  }

  @Test
  @DisplayName("사용자 논리삭제 성공")
  void softDeleteUser_Success() throws Exception {
    //when & then
    mockMvc.perform(
        delete("/api/users/{userId}", userId)
    )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("사용자 물리 삭제 성공")
  void hardDeeleteUser_Success() throws Exception {
    //when & then
    mockMvc.perform(
        delete("/api/users/{userId}/hard", userId)
    )
        .andExpect(status().isNoContent());
  }

  private static User createUser() {
    return User.builder()
        .email("test@test.com")
        .nickname("test")
        .password("test")
        .userStatus(UserStatus.ACTIVE)
        .deletedAt(null)
        .subscriptions(new ArrayList<>())
        .comments(new ArrayList<>())
        .commentLikes(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .articlesViewUsers(new ArrayList<>())
        .build();
  }


}
