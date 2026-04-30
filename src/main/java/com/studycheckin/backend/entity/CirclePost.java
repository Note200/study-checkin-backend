package com.studycheckin.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("circle_post")
public class CirclePost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private Integer likeCount;
    private Integer commentCount;
    private Integer type;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
