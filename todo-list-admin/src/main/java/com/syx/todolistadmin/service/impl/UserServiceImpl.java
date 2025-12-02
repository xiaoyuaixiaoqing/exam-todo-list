package com.syx.todolistadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syx.todolistadmin.entity.User;
import com.syx.todolistadmin.mapper.UserMapper;
import com.syx.todolistadmin.service.UserService;
import com.syx.todolistadmin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public User register(String username, String password, String email, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 用户登录成功，在Redis中记录其在线状态，有效期为24小时
        String onlineKey = "user:online:" + user.getId();
        redisTemplate.opsForValue().set(onlineKey, "1", 24, TimeUnit.HOURS);
        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
