package com.syx.todolistadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_log")
public class TaskLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    private Long userId;
    private String action;
    private String oldValue;
    private String newValue;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
