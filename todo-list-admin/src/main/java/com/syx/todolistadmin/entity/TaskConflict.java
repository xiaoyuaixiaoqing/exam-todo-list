package com.syx.todolistadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_conflict")
public class TaskConflict {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    private Long userId;
    private String conflictType;
    private String conflictData;
    private Integer resolved;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
