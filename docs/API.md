# API 概览

统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 认证

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/api/auth/login` | 学生/管理员登录 |
| GET | `/api/auth/captcha` | 演示验证码 |

## 学生端

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/api/student/profile` | 当前学生资料、流程阶段、宿舍和审核记录 |
| GET | `/api/student/qualification` | 当前学生资格信息 |
| POST | `/api/student/qualification/apply` | 提交资格修改申请 |
| GET | `/api/student/payment/items` | 缴费项目和当前缴费状态 |
| POST | `/api/student/payment` | 模拟支付 |
| GET | `/api/student/dorm` | 当前宿舍 |
| POST | `/api/student/dorm/assign` | 自动分配宿舍 |
| POST | `/api/student/checkin` | 学生确认现场报到 |
| GET | `/api/student/announcements` | 学生查看已发布公告 |

## 管理端

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/api/admin/dashboard/stats` | 数据看板 |
| GET/POST | `/api/admin/students` | 学生分页/新增 |
| PUT/DELETE | `/api/admin/students/{id}` | 编辑/删除学生 |
| PUT | `/api/admin/students/{id}/reset-password` | 重置密码为 123456 |
| PUT | `/api/admin/students/{id}/checkin` | 管理员确认报到 |
| GET/POST | `/api/admin/dorm/buildings` | 楼栋列表/新增 |
| PUT/DELETE | `/api/admin/dorm/buildings/{id}` | 编辑/删除楼栋 |
| GET | `/api/admin/dorm/rooms` | 房间列表 |
| POST | `/api/admin/dorm/rooms/batch` | 批量生成房间和床位 |
| DELETE | `/api/admin/dorm/rooms/{id}` | 删除空房间 |
| GET | `/api/admin/dorm/occupancy` | 床位占用树 |
| GET/POST | `/api/admin/fees` | 缴费项目分页/新增 |
| PUT/DELETE | `/api/admin/fees/{id}` | 编辑/删除缴费项目 |
| GET | `/api/admin/modifications/list` | 修改申请分页 |
| PUT | `/api/admin/modifications/approve/{id}` | 审核通过或驳回 |
| GET/POST | `/api/admin/announcements` | 公告分页/新增 |
| PUT/DELETE | `/api/admin/announcements/{id}` | 编辑/删除公告 |
