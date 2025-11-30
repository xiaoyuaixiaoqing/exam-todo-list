package com.syx.todolistadmin.service;

import com.syx.todolistadmin.entity.User;

public interface UserService {
    User register(String username, String password, String email, String phone);
    String login(String username, String password);
    User getById(Long id);
}
