# 高校迎新管理系统

基于 Spring Boot 3、Vue 3、Element Plus 和 MySQL 8 的前后端分离迎新管理系统。系统覆盖学生端四步报到流程，以及管理员端学生、宿舍、缴费项目、资格修改审核、公告和数据看板 6 大模块。

## 核心规则

- 学生使用学号、密码和验证码登录；管理员为单级权限。
- 报到流程为资格核验、缴费、宿舍分配、现场报到。
- 缴费为模拟支付，必须完成全部必缴项目后才能分配宿舍。
- 宿舍按性别隔离，并按专业自动分配；优先填满所有楼栋里同专业同楼性别的空房间，没有空房则在第一个匹配性别的楼栋自动创建 4 人间。
- 宿舍结构为楼栋、房间、床位三级。
- 资格修改只允许姓名、学院、专业、班级、手机号、身份证号、家庭地址 7 个字段，审核通过后自动同步学生信息。

## 目录结构

```text
backend/    Spring Boot 后端
frontend/   Vue 3 前端
database/   MySQL 初始化脚本
docs/       项目文档
```

## 演示账号

| 角色 | 账号 | 密码 |
| :--- | :--- | :--- |
| 管理员 | admin | 123456 |
| 学生 | 20260001 | 123456 |
| 学生 | 20260003 | 123456 |

验证码固定填写 `6666`。

## 数据库初始化

1. 启动 MySQL 8。
2. 执行 `database/schema.sql`。
3. 如本地数据库账号不是 `root/root`，修改 `backend/src/main/resources/application.yml` 中的 datasource 配置。

## 后端启动

```powershell
cd backend
C:\Maven\apache-maven-3.8.2\bin\mvn.cmd spring-boot:run
```

后端默认监听 `http://localhost:8080`。

## 前端启动

```powershell
cd frontend
npm.cmd install --cache .\.npm-cache
npm.cmd --cache .\.npm-cache run dev
```

前端默认访问 `http://localhost:5173`，Vite 会把 `/api` 代理到 `http://localhost:8080`。

## 构建检查

```powershell
cd backend
C:\Maven\apache-maven-3.8.2\bin\mvn.cmd test

cd ../frontend
npm.cmd --cache .\.npm-cache run build
```
