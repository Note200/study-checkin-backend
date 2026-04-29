package com.studycheckin.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级圈动态表
 */
@Data
@TableName("circle_post")
public class CirclePost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    /** 0=普通 1=学习分享 2=提问 3=资源推荐 */
    private Integer type;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
