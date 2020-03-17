package com.lq.gmall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author tianmeng
 * @date 2020/3/8
 */
@Data
@ConfigurationProperties(prefix = "gmall.thread")
@Component
@Configuration
public class ThreadProperties {
    /**
     * gmall.thread.corePoolSize=8
     * gmall.thread.maximumPoolSize=100
     * gmall.thread.queue=100000
     */

    private Integer corePoolSize;

    private Integer maximumPoolSize;

    private Integer queue;

}
