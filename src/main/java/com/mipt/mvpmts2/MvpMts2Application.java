package com.mipt.mvpmts2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MvpMts2Application {

  public static void main(String[] args) {
    SpringApplication.run(MvpMts2Application.class, args);
  }
}
