-- ============================================
-- 大学生学习管理与打卡系统 - 数据库初始化脚本
-- 执行顺序：1 → 2 → 3
-- ============================================

-- ============================================
-- 第1步：创建数据库
-- ============================================
DROP DATABASE IF EXISTS `study_checkin`;
CREATE DATABASE `study_checkin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `study_checkin`;

-- ============================================
-- 第2步：创建数据表
-- ============================================

-- 用户表
CREATE TABLE `user` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(100) DEFAULT NULL COMMENT '微信openid',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名/学号',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `class_id` INT DEFAULT NULL COMMENT '班级ID',
  `role` TINYINT DEFAULT 0 COMMENT '角色: 0=学生, 1=管理员',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0=正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 班级表
CREATE TABLE `class` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name` VARCHAR(100) NOT NULL COMMENT '班级名称',
  `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
  `creator_id` INT DEFAULT NULL COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 课程表
CREATE TABLE `course` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `teacher` VARCHAR(50) DEFAULT NULL COMMENT '授课教师',
  `classroom` VARCHAR(50) DEFAULT NULL COMMENT '上课教室',
  `week_day` TINYINT NOT NULL COMMENT '星期几(1-7)',
  `start_section` TINYINT NOT NULL COMMENT '开始节次',
  `end_section` TINYINT NOT NULL COMMENT '结束节次',
  `start_week` TINYINT DEFAULT 1 COMMENT '开始周次',
  `end_week` TINYINT DEFAULT 18 COMMENT '结束周次',
  `color` VARCHAR(20) DEFAULT '#4A90E2' COMMENT '显示颜色',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 学习计划表
CREATE TABLE `study_plan` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '计划标题',
  `content` TEXT DEFAULT NULL COMMENT '计划内容',
  `plan_date` DATE NOT NULL COMMENT '计划日期',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0=未完成, 1=已完成',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习计划表';

-- 打卡任务表
CREATE TABLE `checkin_task` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `type` TINYINT DEFAULT 0 COMMENT '类型: 0=习惯, 1=目标',
  `target_days` INT DEFAULT 30 COMMENT '目标天数',
  `is_public` TINYINT DEFAULT 0 COMMENT '是否公开: 0=私密, 1=班级公开',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡任务表';

-- 打卡记录表
CREATE TABLE `checkin_record` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_id` INT NOT NULL COMMENT '任务ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `checkin_date` DATE NOT NULL COMMENT '打卡日期',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '打卡备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_user_date` (`task_id`, `user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 公告表
CREATE TABLE `notice` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `admin_id` INT DEFAULT NULL COMMENT '发布者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================
-- 第3步：插入测试数据
-- ============================================

-- ① 班级数据（2个班级）
INSERT INTO `class` (`name`, `invite_code`, `creator_id`) VALUES
('2024级计算机应用技术2班', 'CLASS001', 1),
('2024级软件技术1班', 'CLASS002', 1);

-- ② 用户数据（包含账号密码登录，密码使用 BCrypt 加密）
INSERT INTO `user` (`openid`, `username`, `password`, `nickname`, `avatar`, `major`, `class_id`, `role`, `status`) VALUES
('admin_openid_001', 'gongpeng', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzyG', '龚鹏', 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4Fbnp6ERQySc2LhheBOJtQVCSYVic3COEcrLoN8ib8A/0', '计算机应用技术', 1, 1, 0);
-- 注意：密码为 '123456' 的 BCrypt 哈希值

INSERT INTO `user` (`openid`, `nickname`, `avatar`, `major`, `class_id`, `role`, `status`) VALUES
('fake_openid_001', '张小明', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_002', '李美华', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_003', '王建国', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_004', '陈晓丽', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_005', '刘志远', 'https://thispersondoesnotexist.com/image', '软件技术', 2, 0, 0);

-- ③ 课程表（属于用户1，覆盖周一到周五）
INSERT INTO `course` (`user_id`, `name`, `teacher`, `classroom`, `week_day`, `start_section`, `end_section`, `start_week`, `end_week`, `color`) VALUES
(1, 'Java程序设计', '陈老师', 'A301', 1, 1, 2, 1, 18, '#4A90E2'),
(1, '数据库原理', '林老师', 'B205', 1, 3, 4, 1, 18, '#67C23A'),
(1, 'Web前端开发', '吴老师', 'C102', 2, 1, 2, 1, 18, '#E6A23C'),
(1, '计算机网络', '黄老师', 'A401', 2, 5, 6, 1, 18, '#F56C6C'),
(1, '操作系统', '郑老师', 'B303', 3, 3, 4, 1, 18, '#9B59B6'),
(1, '软件工程', '张老师', 'A201', 3, 7, 8, 1, 18, '#1ABC9C'),
(1, '数据结构', '王老师', 'C301', 4, 1, 2, 1, 18, '#E6A23C'),
(1, 'Python编程', '李老师', 'B101', 4, 5, 6, 1, 18, '#4A90E2'),
(1, '毕业设计指导', '杨老师', 'A501', 5, 3, 4, 10, 20, '#F56C6C'),
(1, '英语', '刘老师', 'C201', 5, 7, 8, 1, 18, '#909399');

-- ④ 学习计划
INSERT INTO `study_plan` (`user_id`, `title`, `content`, `plan_date`, `status`) VALUES
(1, '完成Java作业第3章练习', '完成教材P88-P102所有习题，重点掌握集合框架', CURDATE() - INTERVAL 6 DAY, 1),
(1, '复习数据库SQL语句', '重点复习JOIN、子查询、事务处理相关知识点', CURDATE() - INTERVAL 5 DAY, 1),
(1, '前端Vue框架学习', '学习Vue组件通信、生命周期钩子', CURDATE() - INTERVAL 4 DAY, 1),
(1, '刷LeetCode 5道题', '数组和链表相关算法题，中等难度', CURDATE() - INTERVAL 3 DAY, 1),
(1, '撰写毕设开题报告', '完成研究背景、意义、技术路线部分', CURDATE() - INTERVAL 2 DAY, 1),
(1, '整理课堂笔记', '整理本周操作系统和软件工程的课堂笔记', CURDATE() - INTERVAL 1 DAY, 0),
(1, '完成毕设需求分析文档', '完成系统功能模块划分和用例图', CURDATE(), 0),
(1, '预习下周Java内容', '预习第7章：多线程与并发编程', CURDATE() + INTERVAL 1 DAY, 0);

-- ⑤ 打卡任务（3个）
INSERT INTO `checkin_task` (`user_id`, `title`, `type`, `target_days`, `is_public`) VALUES
(1, '每日背单词', 0, 30, 1),
(1, '坚持跑步锻炼', 1, 21, 1),
(1, '毕设每日进度', 0, 60, 1);

-- ⑥ 打卡记录
INSERT INTO `checkin_record` (`task_id`, `user_id`, `checkin_date`, `remark`) VALUES
(1, 1, CURDATE() - INTERVAL 13 DAY, '今天背了30个单词，CET-4核心词汇'),
(1, 1, CURDATE() - INTERVAL 12 DAY, '背了25个单词，有点累但坚持了'),
(1, 1, CURDATE() - INTERVAL 11 DAY, '打卡！背单词30个'),
(1, 1, CURDATE() - INTERVAL 10 DAY, '坚持！今日词汇：计算机专业术语'),
(1, 1, CURDATE() - INTERVAL 9 DAY, '背了四级高频词30个'),
(1, 1, CURDATE() - INTERVAL 8 DAY, '今天背了40个，超额完成'),
(1, 1, CURDATE() - INTERVAL 6 DAY, '继续坚持，背了30个'),
(1, 1, CURDATE() - INTERVAL 5 DAY, '今天背单词啦'),
(1, 1, CURDATE() - INTERVAL 4 DAY, '背了30个单词+复习昨天的'),
(1, 1, CURDATE() - INTERVAL 3 DAY, '打卡第10天！坚持就是胜利'),
(1, 1, CURDATE() - INTERVAL 2 DAY, '今天背了35个'),
(1, 1, CURDATE() - INTERVAL 1 DAY, '背单词打卡'),
(1, 1, CURDATE(), '今日打卡完成！');

INSERT INTO `checkin_record` (`task_id`, `user_id`, `checkin_date`, `remark`) VALUES
(2, 1, CURDATE() - INTERVAL 10 DAY, '跑了3公里，配速6分钟'),
(2, 1, CURDATE() - INTERVAL 9 DAY, '跑了3.5公里'),
(2, 1, CURDATE() - INTERVAL 8 DAY, '今天跑了4公里，新纪录！'),
(2, 1, CURDATE() - INTERVAL 6 DAY, '雨天在宿舍做了20分钟跳绳'),
(2, 1, CURDATE() - INTERVAL 5 DAY, '跑步3公里打卡'),
(2, 1, CURDATE() - INTERVAL 3 DAY, '跑了5公里！状态很好'),
(2, 1, CURDATE() - INTERVAL 1 DAY, '跑步打卡，3公里'),
(2, 1, CURDATE(), '今天跑了4公里');

INSERT INTO `checkin_record` (`task_id`, `user_id`, `checkin_date`, `remark`) VALUES
(3, 1, CURDATE() - INTERVAL 4 DAY, '完成了数据库设计，7张表'),
(3, 1, CURDATE() - INTERVAL 3 DAY, '搭好了Spring Boot后端骨架'),
(3, 1, CURDATE() - INTERVAL 2 DAY, '完成了用户登录功能'),
(3, 1, CURDATE() - INTERVAL 1 DAY, '完成了课程表和学习计划模块'),
(3, 1, CURDATE(), '今天完成了打卡模块，进度顺利');

-- ⑦ 公告数据
INSERT INTO `notice` (`title`, `content`, `admin_id`) VALUES
('关于毕业设计中期检查安排的通知', '各位同学，毕业设计中期检查将于2026年4月15日进行，请各位同学做好准备，提前整理好开发文档和系统演示材料。具体时间安排另行通知，请关注班级群公告。', 1),
('五一假期放假通知', '根据学校安排，五一劳动节假期为2026年5月1日至5月5日，共5天。节后第一天正常上课，请同学们合理安排假期时间，注意安全。', 1),
('期末考试时间安排', '本学期期末考试安排在2026年6月下旬，具体科目时间请关注教务系统，请同学们提前准备，认真复习。祝大家取得好成绩！', 1);

-- ============================================
-- 完成检查
-- ============================================
SELECT '========== 数据库初始化完成 ==========' AS `状态`;
SELECT CONCAT('班级数: ', COUNT(*)) AS 统计 FROM `class`
UNION ALL SELECT CONCAT('用户数: ', COUNT(*)) FROM `user`
UNION ALL SELECT CONCAT('课程数: ', COUNT(*)) FROM `course`
UNION ALL SELECT CONCAT('计划数: ', COUNT(*)) FROM `study_plan`
UNION ALL SELECT CONCAT('打卡任务数: ', COUNT(*)) FROM `checkin_task`
UNION ALL SELECT CONCAT('打卡记录数: ', COUNT(*)) FROM `checkin_record`
UNION ALL SELECT CONCAT('公告数: ', COUNT(*)) FROM `notice`;
