CREATE DATABASE IF NOT EXISTS campus_onboarding
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE campus_onboarding;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS majors;
DROP TABLE IF EXISTS colleges;
DROP TABLE IF EXISTS checkin_records;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS qualification_modifications;
DROP TABLE IF EXISTS payment_records;
DROP TABLE IF EXISTS fee_items;
DROP TABLE IF EXISTS dorm_beds;
DROP TABLE IF EXISTS dorm_rooms;
DROP TABLE IF EXISTS dorm_buildings;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS admins;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE colleges (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  sort_no INT NOT NULL DEFAULT 0,
  is_enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_college_sort (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE majors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  college_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  is_enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_college_major (college_id, name),
  INDEX idx_major_college_sort (college_id, sort_no),
  CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES colleges(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  college VARCHAR(100) NOT NULL,
  major VARCHAR(100) NOT NULL,
  class_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30),
  id_card VARCHAR(30),
  address VARCHAR(255),
  password VARCHAR(100) NOT NULL,
  is_paid TINYINT(1) NOT NULL DEFAULT 0,
  is_checked_in TINYINT(1) NOT NULL DEFAULT 0,
  bed_id BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_student_major_gender (major, gender),
  INDEX idx_student_college (college),
  INDEX idx_student_status (is_paid, is_checked_in)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dorm_buildings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building_no VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_building_gender_sort (gender, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dorm_rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building_id BIGINT NOT NULL,
  room_no VARCHAR(30) NOT NULL,
  capacity INT NOT NULL DEFAULT 4,
  occupied_count INT NOT NULL DEFAULT 0,
  major VARCHAR(100) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_building_room (building_id, room_no),
  INDEX idx_room_assign (gender, major, occupied_count),
  CONSTRAINT fk_room_building FOREIGN KEY (building_id) REFERENCES dorm_buildings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dorm_beds (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id BIGINT NOT NULL,
  bed_no VARCHAR(30) NOT NULL,
  is_occupied TINYINT(1) NOT NULL DEFAULT 0,
  student_id VARCHAR(30),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_room_bed (room_id, bed_no),
  UNIQUE KEY uk_bed_student (student_id),
  INDEX idx_bed_room_occupied (room_id, is_occupied),
  CONSTRAINT fk_bed_room FOREIGN KEY (room_id) REFERENCES dorm_rooms(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fee_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  is_required TINYINT(1) NOT NULL DEFAULT 1,
  is_enabled TINYINT(1) NOT NULL DEFAULT 1,
  description VARCHAR(255),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id VARCHAR(30) NOT NULL,
  fee_item_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PAID',
  pay_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student_fee (student_id, fee_item_id),
  INDEX idx_payment_time (pay_time),
  CONSTRAINT fk_payment_fee FOREIGN KEY (fee_item_id) REFERENCES fee_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qualification_modifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id VARCHAR(30) NOT NULL,
  field_name VARCHAR(50) NOT NULL,
  field_label VARCHAR(50) NOT NULL,
  old_value VARCHAR(255),
  new_value VARCHAR(255) NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  review_comment VARCHAR(500),
  review_time DATETIME,
  reviewer VARCHAR(50),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_mod_student_status (student_id, status),
  INDEX idx_mod_status_time (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE announcements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  is_published TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_announcement_publish_time (is_published, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE checkin_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id VARCHAR(30) NOT NULL,
  operator VARCHAR(50) NOT NULL,
  remark VARCHAR(255),
  checkin_time DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_checkin_student (student_id),
  INDEX idx_checkin_time (checkin_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @pwd = '$2b$12$izEA9IQSVcF3M90z8rPBdue/yK8ka4vCq1QWRStZeqZP2mb2qwYQm';

INSERT INTO admins (id, username, name, password) VALUES
(1, 'admin', '迎新管理员', @pwd);

INSERT INTO colleges (id, name, sort_no, is_enabled) VALUES
(1, '计算机学院', 1, 1),
(2, '经济管理学院', 2, 1),
(3, '外国语学院', 3, 1);

INSERT INTO majors (id, college_id, name, sort_no, is_enabled) VALUES
(1, 1, '软件工程', 1, 1),
(2, 1, '计算机科学与技术', 2, 1),
(3, 1, '数据科学与大数据技术', 3, 1),
(4, 2, '会计学', 1, 1),
(5, 2, '财务管理', 2, 1),
(6, 2, '工商管理', 3, 1),
(7, 3, '英语', 1, 1),
(8, 3, '商务英语', 2, 1),
(9, 3, '日语', 3, 1);

INSERT INTO dorm_buildings (id, building_no, name, gender, sort_no) VALUES
(1, 'M1', '男生一号楼', '男', 1),
(2, 'M2', '男生二号楼', '男', 2),
(3, 'F1', '女生一号楼', '女', 1),
(4, 'F2', '女生二号楼', '女', 2);

INSERT INTO dorm_rooms (id, building_id, room_no, capacity, occupied_count, major, gender) VALUES
(1, 1, '101', 4, 2, '软件工程', '男'),
(2, 1, '102', 4, 0, '计算机科学与技术', '男'),
(3, 3, '201', 4, 1, '软件工程', '女'),
(4, 3, '202', 4, 1, '英语', '女'),
(5, 3, '203', 4, 0, '会计学', '女');

INSERT INTO dorm_beds (id, room_id, bed_no, is_occupied, student_id) VALUES
(1, 1, '1', 1, '20260001'),
(2, 1, '2', 1, '20260002'),
(3, 1, '3', 0, NULL),
(4, 1, '4', 0, NULL),
(5, 2, '1', 0, NULL),
(6, 2, '2', 0, NULL),
(7, 2, '3', 0, NULL),
(8, 2, '4', 0, NULL),
(9, 3, '1', 1, '20260004'),
(10, 3, '2', 0, NULL),
(11, 3, '3', 0, NULL),
(12, 3, '4', 0, NULL),
(13, 4, '1', 1, '20260006'),
(14, 4, '2', 0, NULL),
(15, 4, '3', 0, NULL),
(16, 4, '4', 0, NULL),
(17, 5, '1', 0, NULL),
(18, 5, '2', 0, NULL),
(19, 5, '3', 0, NULL),
(20, 5, '4', 0, NULL);

INSERT INTO students
(id, student_id, name, gender, college, major, class_name, phone, id_card, address, password, is_paid, is_checked_in, bed_id)
VALUES
(1, '20260001', '张明', '男', '计算机学院', '软件工程', '软工2601', '13800000001', '320100200801010011', '江苏省南京市', @pwd, 1, 1, 1),
(2, '20260002', '李强', '男', '计算机学院', '软件工程', '软工2601', '13800000002', '320100200801010022', '江苏省苏州市', @pwd, 1, 0, 2),
(3, '20260003', '王磊', '男', '计算机学院', '计算机科学与技术', '计科2601', '13800000003', '320100200801010033', '安徽省合肥市', @pwd, 0, 0, NULL),
(4, '20260004', '陈晨', '女', '计算机学院', '软件工程', '软工2602', '13800000004', '320100200801010044', '浙江省杭州市', @pwd, 1, 0, 9),
(5, '20260005', '刘雨', '女', '经济管理学院', '会计学', '会计2601', '13800000005', '320100200801010055', '山东省济南市', @pwd, 0, 0, NULL),
(6, '20260006', '赵琳', '女', '外国语学院', '英语', '英语2601', '13800000006', '320100200801010066', '上海市浦东新区', @pwd, 1, 1, 13);

INSERT INTO fee_items (id, name, amount, is_required, is_enabled, description) VALUES
(1, '学费', 5800.00, 1, 1, '本科新生学年学费'),
(2, '住宿费', 1200.00, 1, 1, '四人间住宿费'),
(3, '大学生医保', 380.00, 0, 1, '可选缴纳项目'),
(4, '校园卡预存款', 100.00, 0, 1, '入校消费预存');

INSERT INTO payment_records (student_id, fee_item_id, amount, status, pay_time) VALUES
('20260001', 1, 5800.00, 'PAID', '2026-07-13 09:30:00'),
('20260001', 2, 1200.00, 'PAID', '2026-07-13 09:31:00'),
('20260002', 1, 5800.00, 'PAID', '2026-07-14 10:15:00'),
('20260002', 2, 1200.00, 'PAID', '2026-07-14 10:16:00'),
('20260004', 1, 5800.00, 'PAID', '2026-07-17 14:20:00'),
('20260004', 2, 1200.00, 'PAID', '2026-07-17 14:21:00'),
('20260006', 1, 5800.00, 'PAID', '2026-07-18 11:08:00'),
('20260006', 2, 1200.00, 'PAID', '2026-07-18 11:09:00'),
('20260006', 4, 100.00, 'PAID', '2026-07-18 11:10:00');

INSERT INTO qualification_modifications
(student_id, field_name, field_label, old_value, new_value, reason, status, create_time)
VALUES
('20260003', 'phone', '手机号', '13800000003', '13900000003', '录取信息中的联系电话已经停用，需要改为当前手机号。', 'PENDING', '2026-07-18 15:20:00');

INSERT INTO announcements (title, content, is_published, create_time) VALUES
('2026级新生报到须知', '请新生按系统流程完成资格核验、缴费、宿舍分配，并携带录取通知书和身份证到校报到。', 1, '2026-07-15 08:00:00'),
('校园卡领取安排', '完成现场报到后，可在学院迎新点领取校园卡和入学资料袋。', 1, '2026-07-16 09:00:00'),
('绿色通道说明', '家庭经济困难学生可到现场绿色通道办理缓缴申请，系统缴费状态由管理员后续维护。', 1, '2026-07-17 10:00:00');

INSERT INTO checkin_records (student_id, operator, remark, checkin_time) VALUES
('20260001', '20260001', '学生端确认到校', '2026-07-18 09:00:00'),
('20260006', '20260006', '学生端确认到校', '2026-07-18 10:30:00');
