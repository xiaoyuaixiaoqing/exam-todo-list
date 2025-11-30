package com.syx.todolistadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_member")
public class TeamMember {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long teamId;
    private Long userId;
    private Integer role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
