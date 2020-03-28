package com.lq.rabbit.controller;

import com.lq.rabbit.entity.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author tianmeng
 * @date 2020/3/17
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/create")
    public Order createOrder(Integer skuId,Integer memberId,Integer num){
        Order order = new Order(Long.parseLong(UUID.randomUUID().toString().replace("-","")),skuId,memberId,num);
        //rabbitTemplate.convertAndSend("order-exchange","create-order",order);

        rabbitTemplate.convertAndSend("user-order-delay-exchange","order_delay",order);

        return order;
    }



}
