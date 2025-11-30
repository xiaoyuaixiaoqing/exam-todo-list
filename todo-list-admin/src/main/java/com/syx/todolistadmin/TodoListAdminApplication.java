package com.syx.todolistadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TodoListAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoListAdminApplication.class, args);
    }

}
