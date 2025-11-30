-- TODO List 数据库表结构

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 1 COMMENT '逻辑删除（1未删除，0已删除）',
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`),
  INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 任务表
CREATE TABLE IF NOT EXISTS `todo_task` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '任务ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `description` TEXT COMMENT '任务描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待办，1进行中，2已完成）',
  `priority` TINYINT NOT NULL DEFAULT 1 COMMENT '优先级（1低，2中，3高）',
  `due_date` DATETIME COMMENT '截止日期',
  `category` VARCHAR(50) COMMENT '分类（工作/学习/生活）',
  `tags` VARCHAR(500) COMMENT '标签（JSON数组）',
  `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `team_id` BIGINT COMMENT '所属团队ID',
  `locked_by` BIGINT COMMENT '锁定用户ID',
  `lock_time` DATETIME COMMENT '锁定时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  `is_deleted` TINYINT NOT NULL DEFAULT 1 COMMENT '逻辑删除（1未删除，0已删除）',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_team_id` (`team_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_due_date` (`due_date`),
  INDEX `idx_category` (`category`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_locked_by` (`locked_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 3. 团队表
CREATE TABLE IF NOT EXISTS `team` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '团队ID',
  `name` VARCHAR(100) NOT NULL COMMENT '团队名称',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `description` VARCHAR(500) COMMENT '团队描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 1 COMMENT '逻辑删除',
  INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 4. 团队成员表
CREATE TABLE IF NOT EXISTS `team_member` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '成员ID',
  `team_id` BIGINT NOT NULL COMMENT '团队ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色（1成员，2管理员，3所有者）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  UNIQUE KEY `uk_team_user` (`team_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

-- 5. 操作日志表
CREATE TABLE IF NOT EXISTS `task_log` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '日志ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE/COMPLETE等）',
  `old_value` TEXT COMMENT '旧值（JSON）',
  `new_value` TEXT COMMENT '新值（JSON）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 6. 回收站表
CREATE TABLE IF NOT EXISTS `task_recycle` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '回收站ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `deleted_by` BIGINT NOT NULL COMMENT '删除用户ID',
  `deleted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '删除时间',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间（删除时间+15天）',
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回收站表';

-- 7. 冲突记录表
CREATE TABLE IF NOT EXISTS `task_conflict` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '冲突ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `user_id` BIGINT NOT NULL COMMENT '冲突用户ID',
  `conflict_type` VARCHAR(50) NOT NULL COMMENT '冲突类型（VERSION/LOCK等）',
  `conflict_data` TEXT COMMENT '冲突数据（JSON）',
  `resolved` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已解决（0未解决，1已解决）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '冲突时间',
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_resolved` (`resolved`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='冲突记录表';

-- 8. 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `task_id` BIGINT COMMENT '关联任务ID',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型（SMS/EMAIL/SYSTEM）',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待发送，1已发送，2发送失败）',
  `send_time` DATETIME COMMENT '发送时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
