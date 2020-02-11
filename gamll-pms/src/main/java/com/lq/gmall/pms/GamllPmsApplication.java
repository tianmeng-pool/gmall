package com.lq.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 1.整合dubbo
 * 2.整合mybatisplus
 *
 * logstash整合：
 * 1.导入logback-logstash-encoderjar包
 * 2.导入日志配置文件
 * 在kibana里面建立好日志的索引,就可以可视化检索
 */

@EnableDubbo
@MapperScan(basePackages = "com.lq.gmall.pms.mapper")
@SpringBootApplication
public class GamllPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamllPmsApplication.class, args);
    }

}
