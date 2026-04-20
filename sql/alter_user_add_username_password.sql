-- ============================================
-- 用户表添加账号密码字段
-- ============================================

USE study_checkin;

-- 添加 username 和 password 字段
ALTER TABLE `user` 
ADD COLUMN `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名/学号' AFTER `openid`,
ADD COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT '密码' AFTER `username`;

-- 为已有用户设置默认密码（123456）
UPDATE `user` SET `password` = '123456' WHERE `password` IS NULL;

-- 创建测试账号（管理员：龚鹏）
INSERT INTO `user` (`openid`, `username`, `password`, `nickname`, `major`, `class_id`, `role`, `status`)
VALUES 
('test_admin_001', 'gongpeng', '123456', '龚鹏', '计算机应用技术', 1, 1, 0);

SELECT 'User表更新完成！' AS `状态`;
SHOW COLUMNS FROM `user`;
