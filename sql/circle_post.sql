-- 班级圈动态表
CREATE TABLE IF NOT EXISTS circle_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    content VARCHAR(500) NOT NULL COMMENT '动态内容',
    type TINYINT DEFAULT 0 COMMENT '0=普通 1=学习分享 2=提问 3=资源推荐',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级圈动态表';
