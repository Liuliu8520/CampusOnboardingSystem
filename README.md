# 高校迎新管理系统

基于 Spring Boot 3、Vue 3、Element Plus 和 MySQL 8 的前后端分离高校迎新管理系统。项目围绕新生入学报到的真实流程设计，覆盖学生端多步骤报到和管理员端迎新业务管理，适合课程设计、毕业设计、答辩演示和简历项目展示。

## 技术栈

| 层级 | 技术 |
| :--- | :--- |
| 后端 | Spring Boot 3、MyBatis-Plus、Spring Validation、BCrypt |
| 前端 | Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、ECharts |
| 数据库 | MySQL 8 |
| 构建工具 | Maven、npm |

## 核心业务规则

- 学生使用学号、密码和验证码登录；管理员为单级管理员。
- 报到流程为资格核验、缴费、宿舍分配、现场报到。
- 资格核验支持学生提交信息修改申请，管理员审核通过后同步学生档案。
- 缴费为模拟支付，按缴费项目记录状态；全部启用的必缴项目完成后，学生才算完成必缴。
- 管理员可在学生管理中查看并维护每个学生具体到缴费项目的状态。
- 宿舍按男女分楼管理，结构为楼栋、房间、床位三级。
- 宿舍分配按专业自动分配：系统先在所有匹配性别楼栋中查找同专业空房，先到先得；没有空房时，在第一个可用楼栋自动创建新房间。
- 学院和专业使用字典表维护，新增学生、筛选学生和宿舍建房均使用下拉选择，避免手输造成名称不一致。

## 功能模块

### 学生端

- 首页：展示欢迎信息、学号、专业、班级和当前流程状态。
- 个人信息：展示姓名、学院、专业、班级、手机号、身份证号、家庭地址等资料，支持提交资格修改申请。
- 缴费页：展示全部缴费项目、金额、必缴/选缴状态和模拟支付操作。
- 报到页：集成宿舍分配和现场报到确认。
- 通知页：查看管理员发布的新生报到通知。

### 管理员端

- 学生管理：学生增删改查、筛选、重置密码、确认报到、项目级缴费状态维护。
- 宿舍管理：楼栋管理、批量生成房间、床位占用图、床位学生姓名和学号展示。
- 缴费项目：配置缴费项目、金额、必缴/选缴、启用/停用。
- 资格修改审核：审核学生提交的信息修改申请。
- 公告管理：发布和维护迎新通知。
- 数据看板：展示学生数量、缴费率、报到率、宿舍分配情况和趋势数据。

## 目录结构

```text
CampusOnboardingSystem/
├─ backend/    Spring Boot 后端服务
├─ frontend/   Vue 3 前端项目
├─ database/   MySQL 初始化脚本
└─ docs/       接口和项目说明文档
```

## 数据库说明

主初始化脚本为：

```text
database/schema.sql
```

脚本会创建并初始化以下核心表：

- `admins`：管理员账号
- `students`：学生档案和报到总状态
- `colleges`：学院字典
- `majors`：专业字典
- `fee_items`：缴费项目
- `payment_records`：学生缴费记录
- `dorm_buildings`：宿舍楼栋
- `dorm_rooms`：宿舍房间
- `dorm_beds`：宿舍床位
- `qualification_modifications`：资格修改申请
- `announcements`：公告通知
- `checkin_records`：现场报到记录

## 演示账号

| 角色 | 账号 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| 管理员 | admin | 123456 | 管理员端演示 |
| 学生 | 20260001 | 123456 | 已缴费、已分配宿舍、已报到 |
| 学生 | 20260003 | 123456 | 未完成缴费，适合演示流程推进 |

验证码固定填写 `6666`。

## 本地启动

### 1. 初始化数据库

1. 启动 MySQL 8。
2. 执行 `database/schema.sql`。
3. 如本地数据库账号不是 `root/root`，修改 `backend/src/main/resources/application.yml` 中的 datasource 配置。

### 2. 启动后端

```powershell
cd backend
C:\Maven\apache-maven-3.8.2\bin\mvn.cmd spring-boot:run
```

后端默认地址为 `http://localhost:8080`。

### 3. 启动前端

```powershell
cd frontend
npm.cmd install --cache .\.npm-cache
npm.cmd --cache .\.npm-cache run dev
```

前端默认地址为 `http://localhost:5173`，Vite 会将 `/api` 代理到 `http://localhost:8080`。

## 构建检查

后端：

```powershell
cd backend
C:\Maven\apache-maven-3.8.2\bin\mvn.cmd test
```

前端：

```powershell
cd frontend
npm.cmd --cache .\.npm-cache run build
```

## 推荐演示流程

1. 使用管理员账号登录，查看数据看板。
2. 在学生管理中新增学生，学院和专业通过下拉选择。
3. 查看学生的必缴汇总和每个缴费项目状态。
4. 在缴费项目中新增或停用项目，再回到学生管理观察项目级状态。
5. 使用学生账号登录，完成个人信息核验、模拟缴费、宿舍分配和现场报到。
6. 回到管理员端宿舍管理，查看床位占用图中的姓名和学号。

## 文档

接口说明见：

```text
docs/API.md
```
