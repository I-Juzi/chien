package com.juzi.chien.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录表实体类
 */
@Data
@TableName("`chien-file_record`")
public class SysFileRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名 */
    private String fileName;

    /** 文件相对路径 */
    private String filePath;

    /** 访问URL */
    private String url;

    /** 文件后缀 */
    private String extension;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 上传用户 */
    private String uploadUser;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
