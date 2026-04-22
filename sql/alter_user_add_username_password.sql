-- ============================================
-- 用户表添加账号密码字段
-- ============================================

USE study_checkin;

-- 添加 username 和 password 字段
ALTER TABLE `user` 
ADD COLUMN `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名/学号' AFTER `openid`,
ADD COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密)' AFTER `username`;

-- 为已有用户设置默认密码（BCrypt 加密后的 123456）
UPDATE `user` SET `password` = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzyG' WHERE `password` IS NULL;

-- 创建测试账号（管理员：龚鹏，密码 123456）
INSERT INTO `user` (`openid`, `username`, `password`, `nickname`, `major`, `class_id`, `role`, `status`)
VALUES 
('test_admin_001', 'gongpeng', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzyG', '龚鹏', '计算机应用技术', 1, 1, 0);

SELECT 'User表更新完成！' AS `状态`;
SHOW COLUMNS FROM `user`;
