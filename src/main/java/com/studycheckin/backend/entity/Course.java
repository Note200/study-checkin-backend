package com.studycheckin.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String teacher;
    private String classroom;
    /** 星期 1=周一 7=周日 */
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;
    private Integer startWeek;
    private Integer endWeek;
    private String color;
    /** 0全周 1单周 2双周 3前8周 4后8周 */
    private Integer weekType;
}
