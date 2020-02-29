package com.lq.gmall.search;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 数组类型的测试，提交需要同名属性多几个值就行
 * 如:localhost:8080/search?catelog=19&catelog=20&catelog=21
 */
@EnableDubbo
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GmallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSearchApplication.class, args);
    }

}
