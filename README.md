# OS Online

[![Java](https://img.shields.io/badge/java8-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.13-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![MyBatis](https://img.shields.io/badge/MyBatis-2.2.2-000000?logo=apache&logoColor=white)](https://mybatis.org/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/zikifaker/os-online)

## 项目介绍

OS Online 是一个基于 SSM
框架开发的仿真操作系统学习网站后端，仿真操作系统的设计参考 [NJAU-OS-course-design-simulated-linux](https://github.com/404874351/NJAU-OS-course-design-simulated-linux)。

用户通过发送指令与仿真操作系统交互，查看系统推送的运行日志，增强对于操作系统基础知识的理解。

## 功能

### 登录鉴权

- 用户注册
- 用户登录
- 用户登出

### 仿真操作系统

- 进程管理：使用多级反馈队列算法
- 内存管理：使用最佳适应算法
- 中断：实现时钟中断、I/O中断、作业请求中断
- 支持的用户指令：开机、启动/暂停、添加随机作业

## 快速开始

1. 执行`src/main/resources/sql`的 SQL 文件，在 MySQL 建立数据表。
2. 启动项目（默认在本地 8088 端口启动），使用 curl 或其他 API 调试工具（例如 [hoppsotch](https://hoppspot.io) 等）进行用户注册。
    ```curl
    curl -X POST http://localhost:8088/user/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "your_username",
        "password": "your_password",
        "email": "your_email"
   }'
    ```
3. 进行用户登录，接口返回 JWT。
    ```curl
    curl -X POST http://localhost:8088/user/login \
        -H "Content-Type: application/json" \
        -d '{
        "username": "your_username",
        "password": "your_password"
    }'
    ```
4. 请求接口 `ws://localhost:8088/ws/os/{sessionId}/?token=your_jwt` 开启一个 OS 会话，其中 `sessionId` 使用随机字符串填写。
5. 建立 WebSocket 连接后，就可以发送用户指令与 OS 进行交互了！在请求体中携带以下格式的 JSON
   数据即可，支持开机（POWER_ON）、启动/暂停（REVERSE_CLOCK）、添加随机作业（REALTIME_JOB）三种指令。
    ```json
    {"command": "POWER_ON"}
    ```