package com.studycheckin.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 微信openid */
    private String openid;
    
    /** 用户名（账号） */
    private String username;
    
    /** 密码（加密存储） */
    private String password;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
    
    /** 专业 */
    private String major;
    
    /** 班级ID */
    private Long classId;
    
    /** 0=学生 1=管理员 */
    private Integer role;
    
    /** 0=正常 1=禁用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
