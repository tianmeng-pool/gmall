package com.lq.rabbit;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 使用RabbitMQ的步骤
 * 1.导入rabbit的相关场景，amqp-starter
 * 2.编写自动配置，RabbitAutoConfiguration,RabbitProperties
 * 3.卡其RabbitMQ功能
 *
 * 自动配置:RabbitTemplate
 */
@EnableRabbit
@SpringBootApplication
public class RabbitTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitTestApplication.class, args);
    }

    /**
     * 自定义RabbitMQtt的MessageConverter
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
