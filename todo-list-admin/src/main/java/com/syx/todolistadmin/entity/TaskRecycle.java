package com.syx.todolistadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_recycle")
public class TaskRecycle {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    private Long deletedBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime deletedTime;
    private LocalDateTime expireTime;
}
