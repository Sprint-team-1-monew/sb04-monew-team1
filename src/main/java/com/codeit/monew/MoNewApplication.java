package com.codeit.monew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoNewApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoNewApplication.class, args);
  }

}
