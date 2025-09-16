package com.codeit.monew.user.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserCleanupTaskletTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserService userService;

  @Mock
  private MeterRegistry meterRegistry;

  @InjectMocks
  private UserCleanupTasklet userCleanupTasklet;

  @Test
  void execute_shouldHandleUserExceptionAndIncrementFailureCounter() {
    // given
    User firstUser = createSoftedDeletedUser(UUID.randomUUID());
    User secondUser = createSoftedDeletedUser(UUID.randomUUID());
    List<User> mockUsers = List.of(firstUser, secondUser);

    given(userRepository.findUsersForHardDelete(any(LocalDateTime.class)))
        .willReturn(mockUsers);

    willDoNothing().given(userService).hardDelete(any(UUID.class));


    StepContribution contribution = mock(StepContribution.class);
    ChunkContext chunkContext = mock(ChunkContext.class);

    Counter mockSuccessCounter = mock(Counter.class);
    Counter mockFailureCounter = mock(Counter.class);

    given(meterRegistry.counter("user.cleanup.success.count"))
        .willReturn(mockSuccessCounter);
    given(meterRegistry.counter("user.cleanup.failure.count"))
        .willReturn(mockFailureCounter);

    // when
    RepeatStatus result = userCleanupTasklet.execute(contribution, chunkContext);

    // then
    then(userRepository).should().findUsersForHardDelete(any(LocalDateTime.class));
    then(userService).should(times(2)).hardDelete(any(UUID.class));
    assertEquals(RepeatStatus.FINISHED, result);
  }

  private User createSoftedDeletedUser(UUID id) {
    User softDeletedUser = User.builder()
        .email("email@email.com")
        .nickname("test")
        .password(BCrypt.withDefaults().hashToString(12, "password".toCharArray()))
        .userStatus(UserStatus.DELETED)
        .deletedAt(LocalDateTime.now().minusMinutes(5))
        .build();
    ReflectionTestUtils.setField(softDeletedUser, "id", id);
    return softDeletedUser;
  }
}
