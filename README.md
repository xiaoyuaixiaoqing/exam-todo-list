# Todo List 待办事项管理系统

## 项目简介
Todo List 是一个基于Spring Boot和Vue3的前后端分离待办事项管理系统，支持任务管理、团队协作、AI语音输入、消息通知等功能。

## 技术栈
### 后端
- Spring Boot 3.5.8
- MyBatis Plus 3.5.11
- Spring Security + JWT
- Redis (缓存和分布式锁)
- MySQL 8.x
- Maven

### 前端
- Vue 3.5.24 + TypeScript
- Vite 7.2.4
- Element Plus 2.11.9
- Pinia 3.0.4 (状态管理)
- Vue Router 4.6.3

## 运行环境
- JDK 17+
- Node.js 16+
- MySQL 8.x
- Redis 5+
- Maven 3.6+
- pnpm 8+

## 快速开始

### 后端运行
1. 创建MySQL数据库并导入sql脚本
   ```bash
   # 创建数据库
   CREATE DATABASE todo_list DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   # 导入表结构和初始数据
   mysql -u root -p todo_list < todo-list-admin/src/main/resources/sql/schema.sql
   mysql -u root -p todo_list < todo-list-admin/src/main/resources/sql/data.sql
   ```

2. 启动Redis服务

3. 修改配置文件
   ```yaml
   # todo-list-admin/src/main/resources/application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/todo_list?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
       username: your_username
       password: your_password
     redis:
       host: localhost
       port: 6379
   ```

4. 启动后端服务
   ```bash
   cd todo-list-admin
   mvn spring-boot:run
   ```
   
   或者使用IDE运行 [TodoListAdminApplication](file:///D:/Java/todo-list/todo-list-admin/src/main/java/com/syx/todolistadmin/TodoListAdminApplication.java#L13-L31)

### 前端运行
1. 安装依赖
   ```bash
   cd todo-list-ui
   pnpm install
   ```

2. 启动开发服务器
   ```bash
   pnpm dev
   ```

3. 访问应用
   打开浏览器访问 http://localhost:5173

## 项目结构
```
todo-list/
├── todo-list-admin/     # 后端项目
│   ├── src/main/java/   # Java源代码
│   │   └── com/syx/todolistadmin/
│   │       ├── controller/  # 控制器层
│   │       ├── service/     # 业务逻辑层
│   │       ├── mapper/      # 数据访问层
│   │       ├── entity/      # 实体类
│   │       └── config/      # 配置类
│   └── src/main/resources/
│       ├── sql/             # 数据库脚本
│       └── application.yml  # 配置文件
└── todo-list-ui/        # 前端项目
    ├── src/
    │   ├── views/       # 页面组件
    │   ├── components/  # 公共组件
    │   ├── api/         # 接口调用
    │   ├── router/      # 路由配置
    │   └── stores/      # 状态管理
    └── index.html       # 入口文件
```

## 功能特性
- ✅ 用户认证与权限控制
- ✅ 任务创建、编辑、删除、状态更新
- ✅ 任务回收站与恢复功能
- ✅ 团队协作与任务分配
- ✅ 任务冲突检测与解决
- ✅ 操作日志记录与审计
- ✅ AI语音输入创建任务
- ✅ 实时消息通知系统
- ✅ 定时任务自动清理过期数据
- ✅ Redis缓存提升系统性能
- ✅ 数据库索引优化查询性能

## 构建部署
### 后端打包
```bash
cd todo-list-admin
mvn clean package
java -jar target/todo-list-admin.jar
```

### 前端构建
```bash
cd todo-list-ui
pnpm build
```
构建产物位于 `dist` 目录，可部署到Nginx等Web服务器。