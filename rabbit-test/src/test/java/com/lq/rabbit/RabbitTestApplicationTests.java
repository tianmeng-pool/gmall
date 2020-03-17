package com.lq.rabbit;

import com.lq.rabbit.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitTestApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 发送消息
     */
    @Test
    void contextLoads() {

        User user = new User("张三","zhangsan@qq.com");

        //给指定的交换机给指定的路由键发送消息
        rabbitTemplate.convertAndSend("direct_exchange","hello_world",user);

        System.out.println("消息发送完成...");
    }

    /**
     * 创建交换机
     */
    @Test
    public void creatExchange(){

        DirectExchange exchange = new DirectExchange("my-exchange",true,false);

        amqpAdmin.declareExchange(exchange);

        System.out.println("交换机创建完成...");
    }

    /**
     * 创建队列
     */
    @Test
    public void test(){

        Queue queue = new Queue("my-queue",true,false,false);

        amqpAdmin.declareQueue(queue);
    }

    /**
     * 创建绑定关系
     */
    @Test
    public void createBinding(){

        /**
         * String destination, 目的地
         * DestinationType destinationType,目的地类型
         * String exchange, 交换机
         * String routingKey,路由键
         * @Nullable Map<String, Object> arguments 参数
         */
        Binding binding = new Binding("my-queue",
                Binding.DestinationType.QUEUE,
                "my-exchange",
                "hello",
                null);

        amqpAdmin.declareBinding(binding);

        System.out.println("绑定完成...");
    }

}
