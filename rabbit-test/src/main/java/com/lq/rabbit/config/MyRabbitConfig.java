package com.lq.rabbit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tianmeng
 * @date 2020/3/16
 */
@Configuration
public class MyRabbitConfig {

    /**
     * 如果RabbitMQ没有这个队列、交换机、绑定关系会自动创建。
     * 队列的唯一标识就是它的名字。
     * 若队列中已经存在这个名字，但是队列的参数不一样，新的队列就会覆盖以前老的队列
     * @return
     */
    @Bean
    public Queue queue(){
        return new Queue("order-queue",true,false,false,null);
    }

    @Bean
    public Exchange orderExchange(){
        return new DirectExchange("order-exchange",true,false);
    }

    @Bean
    public Binding orderBinding(){
        return new Binding("order-queue", Binding.DestinationType.QUEUE,"order-exchange","order-create",null);
    }

}
