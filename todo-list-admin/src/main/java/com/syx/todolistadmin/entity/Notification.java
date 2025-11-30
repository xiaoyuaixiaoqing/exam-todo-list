package com.syx.todolistadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long taskId;
    private String type;
    private String content;
    private Integer status;
    private LocalDateTime sendTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
