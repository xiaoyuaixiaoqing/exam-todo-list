package com.syx.todolistadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syx.todolistadmin.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    @Select("SELECT id,title,description,status,priority,due_date,category,tags,user_id,team_id,locked_by,lock_time,create_time,update_time,version,is_deleted FROM todo_task WHERE id = #{id}")
    List<Task> selectByIdIgnoreLogicDel(Long id);

    @Update("UPDATE todo_task SET is_deleted = 1 WHERE id = #{id}")
    void restoreTask(Long id);
}
