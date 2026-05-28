package com.juzi.chien.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Chien Admin 模块启动类
 * 主要负责用户登录及项目配置管理
 */
@SpringBootApplication
@MapperScan("com.juzi.chien.admin.mapper")
@EnableAsync
public class ChienAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChienAdminApplication.class, args);
    }

}
