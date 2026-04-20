-- ============================================
-- 测试数据插入脚本
-- 当前用户 id = 1
-- ============================================

USE study_checkin;

-- ① 班级数据（2个班级）
INSERT INTO class (name, invite_code, creator_id) VALUES
('2024级计算机应用技术2班', 'CLASS001', 1),
('2024级软件技术1班',       'CLASS002', 1);

-- 把当前用户分配到班级1，设为管理员(role=1)
UPDATE user SET class_id = 1, role = 1, nickname = '龚鹏', major = '计算机应用技术' WHERE id = 1;

-- ② 再插入几个普通同学（模拟班级成员）
INSERT INTO user (openid, nickname, avatar, major, class_id, role, status) VALUES
('fake_openid_001', '张小明', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_002', '李美华', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_003', '王建国', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_004', '陈晓丽', 'https://thispersondoesnotexist.com/image', '计算机应用技术', 1, 0, 0),
('fake_openid_005', '刘志远', 'https://thispersondoesnotexist.com/image', '软件技术', 2, 0, 0);

-- ③ 课程表（属于用户1，覆盖周一到周五）
INSERT INTO course (user_id, name, teacher, classroom, week_day, start_section, end_section, start_week, end_week, color) VALUES
(1, 'Java程序设计', '陈老师', 'A301', 1, 1, 2, 1, 18, '#4A90E2'),
(1, '数据库原理',   '林老师', 'B205', 1, 3, 4, 1, 18, '#67C23A'),
(1, 'Web前端开发',  '吴老师', 'C102', 2, 1, 2, 1, 18, '#E6A23C'),
(1, '计算机网络',   '黄老师', 'A401', 2, 5, 6, 1, 18, '#F56C6C'),
(1, '操作系统',     '郑老师', 'B303', 3, 3, 4, 1, 18, '#9B59B6'),
(1, '软件工程',     '张老师', 'A201', 3, 7, 8, 1, 18, '#1ABC9C'),
(1, '数据结构',     '王老师', 'C301', 4, 1, 2, 1, 18, '#E6A23C'),
(1, 'Python编程',   '李老师', 'B101', 4, 5, 6, 1, 18, '#4A90E2'),
(1, '毕业设计指导', '杨老师', 'A501', 5, 3, 4, 10, 20, '#F56C6C'),
(1, '英语',         '刘老师', 'C201', 5, 7, 8, 1, 18, '#909399');

-- ④ 学习计划（最近7天）
INSERT INTO study_plan (user_id, title, content, plan_date, status) VALUES
(1, '完成Java作业第3章练习',   '完成教材P88-P102所有习题，重点掌握集合框架', CURDATE() - INTERVAL 6 DAY, 1),
(1, '复习数据库SQL语句',       '重点复习JOIN、子查询、事务处理相关知识点',   CURDATE() - INTERVAL 5 DAY, 1),
(1, '前端Vue框架学习',         '学习Vue组件通信、生命周期钩子',             CURDATE() - INTERVAL 4 DAY, 1),
(1, '刷LeetCode 5道题',        '数组和链表相关算法题，中等难度',             CURDATE() - INTERVAL 3 DAY, 1),
(1, '撰写毕设开题报告',        '完成研究背景、意义、技术路线部分',           CURDATE() - INTERVAL 2 DAY, 1),
(1, '整理课堂笔记',            '整理本周操作系统和软件工程的课堂笔记',       CURDATE() - INTERVAL 1 DAY, 0),
(1, '完成毕设需求分析文档',    '完成系统功能模块划分和用例图',               CURDATE(),                  0),
(1, '预习下周Java内容',        '预习第7章：多线程与并发编程',               CURDATE() + INTERVAL 1 DAY, 0);

-- ⑤ 打卡任务（3个，属于用户1，班级可见）
INSERT INTO checkin_task (user_id, title, type, target_days, is_public) VALUES
(1, '每日背单词',     0, 30, 1),
(1, '坚持跑步锻炼',   1, 21, 1),
(1, '毕设每日进度',   0, 60, 1);

-- ⑥ 打卡记录（过去14天，模拟真实打卡）
-- 任务1：背单词（task_id=1，坚持了12天）
INSERT INTO checkin_record (task_id, user_id, checkin_date, remark) VALUES
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
(1, 1, CURDATE(),                   '今日打卡完成！');

-- 任务2：跑步（task_id=2，打卡8天）
INSERT INTO checkin_record (task_id, user_id, checkin_date, remark) VALUES
(2, 1, CURDATE() - INTERVAL 10 DAY, '跑了3公里，配速6分钟'),
(2, 1, CURDATE() - INTERVAL 9 DAY,  '跑了3.5公里'),
(2, 1, CURDATE() - INTERVAL 8 DAY,  '今天跑了4公里，新纪录！'),
(2, 1, CURDATE() - INTERVAL 6 DAY,  '雨天在宿舍做了20分钟跳绳'),
(2, 1, CURDATE() - INTERVAL 5 DAY,  '跑步3公里打卡'),
(2, 1, CURDATE() - INTERVAL 3 DAY,  '跑了5公里！状态很好'),
(2, 1, CURDATE() - INTERVAL 1 DAY,  '跑步打卡，3公里'),
(2, 1, CURDATE(),                    '今天跑了4公里');

-- 任务3：毕设进度（task_id=3，打卡5天）
INSERT INTO checkin_record (task_id, user_id, checkin_date, remark) VALUES
(3, 1, CURDATE() - INTERVAL 4 DAY, '完成了数据库设计，7张表'),
(3, 1, CURDATE() - INTERVAL 3 DAY, '搭好了Spring Boot后端骨架'),
(3, 1, CURDATE() - INTERVAL 2 DAY, '完成了用户登录功能'),
(3, 1, CURDATE() - INTERVAL 1 DAY, '完成了课程表和学习计划模块'),
(3, 1, CURDATE(),                   '今天完成了打卡模块，进度顺利');

-- ⑦ 公告数据
INSERT INTO notice (title, content, admin_id) VALUES
('关于毕业设计中期检查安排的通知', '各位同学，毕业设计中期检查将于2026年4月15日进行，请各位同学做好准备，提前整理好开发文档和系统演示材料。具体时间安排另行通知，请关注班级群公告。', 1),
('五一假期放假通知',               '根据学校安排，五一劳动节假期为2026年5月1日至5月5日，共5天。节后第一天正常上课，请同学们合理安排假期时间，注意安全。', 1),
('期末考试时间安排',               '本学期期末考试安排在2026年6月下旬，具体科目时间请关注教务系统，请同学们提前准备，认真复习。祝大家取得好成绩！', 1);

SELECT '数据插入完成！' AS 状态;
SELECT CONCAT('班级数: ', COUNT(*)) AS 统计 FROM class
UNION ALL SELECT CONCAT('用户数: ', COUNT(*)) FROM user
UNION ALL SELECT CONCAT('课程数: ', COUNT(*)) FROM course
UNION ALL SELECT CONCAT('计划数: ', COUNT(*)) FROM study_plan
UNION ALL SELECT CONCAT('打卡任务数: ', COUNT(*)) FROM checkin_task
UNION ALL SELECT CONCAT('打卡记录数: ', COUNT(*)) FROM checkin_record
UNION ALL SELECT CONCAT('公告数: ', COUNT(*)) FROM notice;
