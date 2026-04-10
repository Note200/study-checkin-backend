# -*- coding: utf-8 -*-
import mysql.connector

conn = mysql.connector.connect(
    host='127.0.0.1',
    port=3306,
    user='root',
    password='root123',
    database='study_checkin',
    charset='utf8mb4'
)
cur = conn.cursor()

print("开始插入测试数据...")

# ① 班级
cur.execute("INSERT IGNORE INTO class (name, invite_code, creator_id) VALUES ('2024计算机应用技术2班', 'CLASS001', 1)")
cur.execute("INSERT IGNORE INTO class (name, invite_code, creator_id) VALUES ('2024软件技术1班', 'CLASS002', 1)")
conn.commit()
cur.execute("SELECT id FROM class WHERE invite_code='CLASS001'")
class1_id = cur.fetchone()[0]
print(f"班级1 id={class1_id}")

# ② 更新当前用户
cur.execute("UPDATE user SET class_id=%s, role=1, nickname='龚鹏', major='计算机应用技术' WHERE id=1", (class1_id,))
conn.commit()
print("当前用户已更新")

# ③ 插入同学
students = [
    ('fake_openid_001', '张小明', '计算机应用技术', class1_id),
    ('fake_openid_002', '李美华', '计算机应用技术', class1_id),
    ('fake_openid_003', '王建国', '计算机应用技术', class1_id),
    ('fake_openid_004', '陈晓丽', '计算机应用技术', class1_id),
    ('fake_openid_005', '刘志远', '软件技术', 2),
]
avatar = 'https://img.icons8.com/color/96/user.png'
for s in students:
    cur.execute(
        "INSERT IGNORE INTO user (openid, nickname, avatar, major, class_id, role, status) VALUES (%s,%s,%s,%s,%s,0,0)",
        (s[0], s[1], avatar, s[2], s[3])
    )
conn.commit()
print("同学数据已插入")

# ④ 课程
courses = [
    (1, 'Java程序设计', '陈老师', 'A301', 1, 1, 2, 1, 18, '#4A90E2'),
    (1, '数据库原理',   '林老师', 'B205', 1, 3, 4, 1, 18, '#67C23A'),
    (1, 'Web前端开发',  '吴老师', 'C102', 2, 1, 2, 1, 18, '#E6A23C'),
    (1, '计算机网络',   '黄老师', 'A401', 2, 5, 6, 1, 18, '#F56C6C'),
    (1, '操作系统',     '郑老师', 'B303', 3, 3, 4, 1, 18, '#9B59B6'),
    (1, '软件工程',     '张老师', 'A201', 3, 7, 8, 1, 18, '#1ABC9C'),
    (1, '数据结构',     '王老师', 'C301', 4, 1, 2, 1, 18, '#E6A23C'),
    (1, 'Python编程',   '李老师', 'B101', 4, 5, 6, 1, 18, '#4A90E2'),
    (1, '毕业设计指导', '杨老师', 'A501', 5, 3, 4, 10, 20,'#F56C6C'),
    (1, '英语',         '刘老师', 'C201', 5, 7, 8, 1, 18, '#909399'),
]
for c in courses:
    cur.execute(
        "INSERT INTO course (user_id,name,teacher,classroom,week_day,start_section,end_section,start_week,end_week,color) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
        c
    )
conn.commit()
print("课程数据已插入")

# ⑤ 学习计划
plans = [
    (1, '完成Java作业第3章练习', '完成教材习题，重点掌握集合框架', 'CURDATE() - INTERVAL 6 DAY', 1),
    (1, '复习数据库SQL语句',     '重点复习JOIN、子查询、事务处理', 'CURDATE() - INTERVAL 5 DAY', 1),
    (1, 'Vue框架学习',           '学习Vue组件通信、生命周期钩子', 'CURDATE() - INTERVAL 4 DAY', 1),
    (1, '刷LeetCode 5道题',      '数组和链表算法题，中等难度',    'CURDATE() - INTERVAL 3 DAY', 1),
    (1, '撰写毕设开题报告',      '完成研究背景、意义、技术路线',  'CURDATE() - INTERVAL 2 DAY', 1),
    (1, '整理课堂笔记',          '整理操作系统和软件工程笔记',    'CURDATE() - INTERVAL 1 DAY', 0),
    (1, '完成毕设需求分析',      '完成系统功能模块划分和用例图',  'CURDATE()',                  0),
    (1, '预习下周Java内容',      '预习多线程与并发编程',          'CURDATE() + INTERVAL 1 DAY', 0),
]
for p in plans:
    cur.execute(
        f"INSERT INTO study_plan (user_id,title,content,plan_date,status) VALUES (%s,%s,%s,{p[3]},%s)",
        (p[0], p[1], p[2], p[4])
    )
conn.commit()
print("学习计划已插入")

# ⑥ 打卡任务
tasks = [
    (1, '每日背单词',   0, 30, 1),
    (1, '坚持跑步锻炼', 1, 21, 1),
    (1, '毕设每日进度', 0, 60, 1),
]
for t in tasks:
    cur.execute(
        "INSERT INTO checkin_task (user_id,title,type,target_days,is_public) VALUES (%s,%s,%s,%s,%s)",
        t
    )
conn.commit()

# 获取任务ID
cur.execute("SELECT id FROM checkin_task WHERE user_id=1 ORDER BY id")
task_ids = [row[0] for row in cur.fetchall()]
t1, t2, t3 = task_ids[0], task_ids[1], task_ids[2]
print(f"打卡任务ID: {t1},{t2},{t3}")

# ⑦ 打卡记录
records = [
    # 背单词 task t1
    (t1, 1, 'CURDATE() - INTERVAL 13 DAY', '今天背了30个单词'),
    (t1, 1, 'CURDATE() - INTERVAL 12 DAY', '背了25个单词'),
    (t1, 1, 'CURDATE() - INTERVAL 11 DAY', '打卡背单词30个'),
    (t1, 1, 'CURDATE() - INTERVAL 10 DAY', '今日词汇：计算机术语'),
    (t1, 1, 'CURDATE() - INTERVAL 9 DAY',  '背了四级高频词30个'),
    (t1, 1, 'CURDATE() - INTERVAL 8 DAY',  '今天背了40个，超额完成'),
    (t1, 1, 'CURDATE() - INTERVAL 6 DAY',  '继续坚持，背了30个'),
    (t1, 1, 'CURDATE() - INTERVAL 5 DAY',  '今天背单词啦'),
    (t1, 1, 'CURDATE() - INTERVAL 4 DAY',  '背了30个单词+复习昨天的'),
    (t1, 1, 'CURDATE() - INTERVAL 3 DAY',  '打卡第10天！坚持就是胜利'),
    (t1, 1, 'CURDATE() - INTERVAL 2 DAY',  '今天背了35个'),
    (t1, 1, 'CURDATE() - INTERVAL 1 DAY',  '背单词打卡'),
    (t1, 1, 'CURDATE()',                    '今日打卡完成！'),
    # 跑步 task t2
    (t2, 1, 'CURDATE() - INTERVAL 10 DAY', '跑了3公里，配速6分钟'),
    (t2, 1, 'CURDATE() - INTERVAL 9 DAY',  '跑了3.5公里'),
    (t2, 1, 'CURDATE() - INTERVAL 8 DAY',  '今天跑了4公里，新纪录！'),
    (t2, 1, 'CURDATE() - INTERVAL 6 DAY',  '做了20分钟跳绳'),
    (t2, 1, 'CURDATE() - INTERVAL 5 DAY',  '跑步3公里打卡'),
    (t2, 1, 'CURDATE() - INTERVAL 3 DAY',  '跑了5公里，状态很好'),
    (t2, 1, 'CURDATE() - INTERVAL 1 DAY',  '跑步打卡，3公里'),
    (t2, 1, 'CURDATE()',                    '今天跑了4公里'),
    # 毕设 task t3
    (t3, 1, 'CURDATE() - INTERVAL 4 DAY',  '完成了数据库设计，7张表'),
    (t3, 1, 'CURDATE() - INTERVAL 3 DAY',  '搭好了SpringBoot后端骨架'),
    (t3, 1, 'CURDATE() - INTERVAL 2 DAY',  '完成了用户登录功能'),
    (t3, 1, 'CURDATE() - INTERVAL 1 DAY',  '完成了课程表和学习计划模块'),
    (t3, 1, 'CURDATE()',                    '今天完成了打卡模块，进度顺利'),
]
for r in records:
    cur.execute(
        f"INSERT INTO checkin_record (task_id,user_id,checkin_date,remark) VALUES (%s,%s,{r[2]},%s)",
        (r[0], r[1], r[3])
    )
conn.commit()
print("打卡记录已插入")

# ⑧ 公告
notices = [
    (1, '关于毕业设计中期检查安排的通知',
     '各位同学，毕业设计中期检查将于2026年4月15日进行，请提前整理好开发文档和系统演示材料。具体时间另行通知，请关注班级群公告。'),
    (1, '五一假期放假通知',
     '根据学校安排，五一劳动节假期为2026年5月1日至5月5日共5天。节后第一天正常上课，请同学们合理安排假期时间，注意安全。'),
    (1, '期末考试时间安排',
     '本学期期末考试安排在2026年6月下旬，具体科目时间请关注教务系统，请同学们提前准备认真复习。祝大家取得好成绩！'),
]
for n in notices:
    cur.execute(
        "INSERT INTO notice (admin_id,title,content) VALUES (%s,%s,%s)",
        n
    )
conn.commit()
print("公告已插入")

# 汇总
cur.execute("SELECT COUNT(*) FROM user")
print(f"用户数: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM course")
print(f"课程数: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM study_plan")
print(f"计划数: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM checkin_task")
print(f"打卡任务数: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM checkin_record")
print(f"打卡记录数: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM notice")
print(f"公告数: {cur.fetchone()[0]}")

cur.close()
conn.close()
print("\n全部完成！")
