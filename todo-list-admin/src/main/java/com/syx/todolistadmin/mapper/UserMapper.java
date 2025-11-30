package com.syx.todolistadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syx.todolistadmin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
