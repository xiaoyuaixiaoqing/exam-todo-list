package com.syx.todolistadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("todo_task")
public class Task {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String description;
    private Integer status;
    private Integer priority;
    private LocalDateTime dueDate;
    private String category;
    private String tags;
    private Long userId;
    private Long teamId;
    private Long lockedBy;
    private LocalDateTime lockTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @Version
    private Integer version;
    @TableLogic(value = "1", delval = "0")
    private Integer isDeleted;
}
