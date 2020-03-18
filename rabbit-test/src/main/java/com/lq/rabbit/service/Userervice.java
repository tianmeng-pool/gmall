package com.lq.rabbit.service;

import com.lq.rabbit.entity.Order;
import com.lq.rabbit.entity.User;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author tianmeng
 * @date 2020/3/15
 */

/**
 * 1、消息确认机制
 *      1、如果这个消息收到了，在处理期间，出现了运行时异常，默认认为消息没有被正确处理，
 *        消息状态为unack；队列中感知都有一个unack的消息。
 *      2、我们不要让他认为是ack还是unack；手动确认机制
 *          否则，场景：
 *              我们收到了消息，并且库存扣了，但是出现了未知的异常，导致消息又重新入队了，
 *              这个消息被不断的重复发给我们；
 *           解决：
 *              1、手动ack；
 *              2、接口的幂等性。在本地维护一个日子表，记录哪些会员哪些商品哪个订单已经减过库存，再来同样的消息就不减了。
 * 2、手动ack。
 *      1、开启手动ack:spring.rabbitmq.listener.simple.acknowledge-mode=manual
 *      2、
 *          public void listener(){
 *              try{
 *                  //处理消息 回复成功
 *                  channel.basicAck();
 *              }catch(Exception e){
 *                  //拒绝消息。所有拒绝了的消息都会重发
 *                  channel.basicNAck()/Reject();
 *              }
 *          }
 */
@Service
public class Userervice {

    /**
     * 方法上可以写一下参数:
     * 1.Message类型:既能获取到消息的内容字节，还能获取到消息的其他属性
     * 2.User user:如果明确我们这个队列以后都是这个类型对象，直接写这个类型参数
     * 3.com.rabbitmq.client.Channel:通道
     * @param message
     */
    @SneakyThrows
    @RabbitListener(queues = "hello")
    public void receiveUserMessage(Message message, User user, Channel channel){
        System.out.println("收到的消息是:" + message);
        byte[] body = message.getBody();
        MessageProperties properties = message.getMessageProperties();
        System.out.println("接收到的消息是:" + user);

        //通过通道拒绝消息  拒绝；可以把消息拒绝掉，让rabbitMQ再发送给别人.
        //channel.basicReject(message.getMessageProperties().getReceivedDelay(),true);
    }

    @SneakyThrows
    @RabbitListener(queues = {"order-queue"})
    public void createOrder(Order order,Message message,Channel channel){
        System.out.println("监听到新的订单生成..." + order);
        Integer skuId = order.getSkuId();
        Integer num = order.getNum();
        System.out.println("库存系统正在扣除[" + skuId + "]商品的数量，此次扣除" + num + "件");

        if (num % 2  == 0) {
            //回复消息处理失败，并且重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            throw new RuntimeException("库存扣除失败");
        }

        System.out.println("扣除成功");
        //回复成功 只回复本条消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @SneakyThrows
    @RabbitListener(queues = {"user-order-queue"})
    public void closeOrder(Order order,Channel channel,Message message){
        System.out.println("正在关闭订单:" + order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
