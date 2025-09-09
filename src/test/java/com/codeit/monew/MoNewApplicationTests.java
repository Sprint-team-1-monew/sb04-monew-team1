package com.codeit.monew;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.task.scheduling.enabled=false",
    "spring.quartz.auto-startup=false"
})
@ActiveProfiles("test")
class MoNewApplicationTests {

  @Test
  void contextLoads() {
  }

}
