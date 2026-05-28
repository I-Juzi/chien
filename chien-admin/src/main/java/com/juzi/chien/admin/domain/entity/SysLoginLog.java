package com.juzi.chien.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("`chien-login_log`")
public class SysLoginLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String ip;
    private String browser;
    private String os;
    private Integer status;
    private String msg;
    private LocalDateTime loginTime;
}
