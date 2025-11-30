package com.syx.todolistadmin.controller;

import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.entity.User;
import com.syx.todolistadmin.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        return Result.success(userService.register(request.getUsername(), request.getPassword(),
                request.getEmail(), request.getPhone()));
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
