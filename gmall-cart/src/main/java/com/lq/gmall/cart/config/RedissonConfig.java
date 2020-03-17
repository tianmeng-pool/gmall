package com.lq.gmall.cart.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tianmeng
 * @date 2020/3/3
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //启用单节点模式
        config.useSingleServer().setAddress("redis://47.115.60.167:6379");
        return Redisson.create(config);
    }

}
