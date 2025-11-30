-- TODO List 初始化数据

-- 插入测试用户（密码: admin123，使用BCrypt加密）
INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`) VALUES
(1, 'admin', '$2a$10$slYQmyNdGzin7olVN3DNeu52nsesvS5rKgB6fvQk1Z3OYfL03xHUC', 'admin@example.com', '13800138000');
