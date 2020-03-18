package com.lq.rabbit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * 一个普通的交换机;
     * 死信队列
     * @return
     */
    @Bean
    public Exchange delayExchange(){
        return new DirectExchange("user-order-delay-exchange",true,false);
    }

    @Bean
    public Queue delayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl",10 * 1000);//这个队列里面所有消息的过期时间
        arguments.put("x-dead-letter-exchange","user-order-exchange");//消息死了交给哪个交换机
        arguments.put("x-dead-letter-routing-key","order");//死信发出去的路由键
        return new Queue("user-order-delay-queue",true,false,false,arguments);
    }

    @Bean
    public Binding delayBinding(){
        return new Binding("user-order-delay-queue",
                Binding.DestinationType.QUEUE,
                "user-order-delay-exchange",
                "order_delay",
                null);
    }

    @Bean
    public Exchange deadExchange(){
        return new DirectExchange("user-order-exchange",true,false);
    }

    @Bean
    public Queue deadQueue(){
        return new Queue("user-order-queue",true,false,false);
    }

    @Bean
    public Binding deadBinding(){
        return new Binding("user-order-queue", Binding.DestinationType.QUEUE,
                "user-order-exchange","order",null);
    }

}
