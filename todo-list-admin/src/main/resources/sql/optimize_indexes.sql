-- 性能优化：为常用查询字段添加索引

-- todo_task表索引优化
ALTER TABLE todo_task ADD INDEX idx_user_id (user_id);
ALTER TABLE todo_task ADD INDEX idx_team_id (team_id);
ALTER TABLE todo_task ADD INDEX idx_status (status);
ALTER TABLE todo_task ADD INDEX idx_due_date (due_date);
ALTER TABLE todo_task ADD INDEX idx_category (category);
ALTER TABLE todo_task ADD INDEX idx_priority (priority);
ALTER TABLE todo_task ADD INDEX idx_create_time (create_time);

-- 复合索引：常用组合查询
ALTER TABLE todo_task ADD INDEX idx_user_status (user_id, status);
ALTER TABLE todo_task ADD INDEX idx_user_category (user_id, category);
ALTER TABLE todo_task ADD INDEX idx_team_status (team_id, status);

-- task_log表索引
ALTER TABLE task_log ADD INDEX idx_task_id (task_id);
ALTER TABLE task_log ADD INDEX idx_user_id (user_id);
ALTER TABLE task_log ADD INDEX idx_create_time (create_time);

-- notification表索引
ALTER TABLE notification ADD INDEX idx_user_id (user_id);
ALTER TABLE notification ADD INDEX idx_task_id (task_id);
ALTER TABLE notification ADD INDEX idx_status (status);
ALTER TABLE notification ADD INDEX idx_send_time (send_time);

-- team_member表索引
ALTER TABLE team_member ADD INDEX idx_team_id (team_id);
ALTER TABLE team_member ADD INDEX idx_user_id (user_id);
ALTER TABLE team_member ADD INDEX idx_team_user (team_id, user_id);

-- task_recycle表索引
ALTER TABLE task_recycle ADD INDEX idx_expire_time (expire_time);
ALTER TABLE task_recycle ADD INDEX idx_deleted_by (deleted_by);

-- team_recycle表索引
ALTER TABLE team_recycle ADD INDEX idx_team_id (team_id);
ALTER TABLE team_recycle ADD INDEX idx_expire_time (expire_time);
