package com.juzi.chien.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {

    /** 上传文件存储路径 */
    private String path = "./uploads";

    /** 允许的文件后缀（逗号分隔） */
    private String allowedExtensions = "jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt,zip";

    /** 单个文件最大大小（MB） */
    private int maxSize = 10;

    /** 单次请求最大大小（MB） */
    private int maxRequestSize = 50;
}
