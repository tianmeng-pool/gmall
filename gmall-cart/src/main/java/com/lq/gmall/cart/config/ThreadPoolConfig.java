package com.lq.gmall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tianmeng
 * @date 2020/3/8
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 主线程池
     * @param properties
     * @return
     */
    @Bean("mainThreadPoolExecutor")
    public ThreadPoolExecutor mainThreadPoolExecutor(ThreadProperties properties){

        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(properties.getCorePoolSize(),
                                    properties.getMaximumPoolSize(), 10,
                                    TimeUnit.MINUTES, queue);
        return threadPoolExecutor;
    }

    /**
     * 从线程池
     * @param properties
     * @return
     */
    @Bean("otherThreadPoolExecutor")
    public ThreadPoolExecutor otherThreadPoolExecutor(ThreadProperties properties){

        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(properties.getCorePoolSize(),
                                    properties.getMaximumPoolSize(), 10,
                                    TimeUnit.MINUTES, queue);
        return threadPoolExecutor;
    }

}
