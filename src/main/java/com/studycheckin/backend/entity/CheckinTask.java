package com.studycheckin.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkin_task")
public class CheckinTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    /** 0=学习 1=阅读 2=背单词 3=运动 4=其他 */
    private Integer type;
    private Integer targetDays;
    private Integer targetMinutes;
    /** 0=私有 1=班级可见 */
    private Integer isPublic;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
