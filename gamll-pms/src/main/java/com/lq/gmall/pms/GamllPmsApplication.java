package com.lq.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 1.整合dubbo
 * 2.整合mybatisplus
 *
 * logstash整合：
 * 1.导入logback-logstash-encoderjar包
 * 2.导入日志配置文件
 * 在kibana里面建立好日志的索引,就可以可视化检索
 */

/**
 * 缓存的使用场景：
 * 一些固定的数据，不太变化的数据，高频访问的数据(基本不变),变换频率低的都可以入缓存，加速系统的访问
 * 缓存的目的：提高系统查询效率，提高性能
 *
 * 1)、将菜单缓存起来，以后查询直接去缓存中拿即可
 *
 * 设计模式：模板模式
 * 操作xxx都有对应的xxxTemplate
 * JdbcTemplcate、RestTemplate、RedisTemplate、MongoTemplate
 *
 * RedisTemplate<Object,Object>;  k-v
 * v有五种类型、Strig、v
 * StringRedisTemplate；k-v都是String的
 *
 * 引入一个场景，猜这个场景的xxxAutoConfiguration,
 * 帮我们注入能操作这个技术的组件，这个场景的配置信息都在xxxProperties中说明了使用哪种前缀配置
 *
 * 2)、整合redis两大步
 *  1.导入starter-data-redis
 *  2.application.properties中配置与spring.redis相关的
 * 注意：
 *      RedisTemplate存数据默认使用jdk的方式序列化存过去；
 *      我们推荐都应该存成json
 *      做法：
 *        将默认的序列化器改为json的
 *
 *  如果发现事务加不上。开启基于注解的事务功能  @EableTransactionManagemet
 *
 *  事务的最终解决方案：
 *      1.普通加事务。导入jdbc-starter。启动类上加@EnableTransactionManagement.方法加@Transactional
 *      2.方法自己调自己类里面的加不上事务
 *          1）、导入aop包，开启代理对象的相关功能
 *              <dependency>
 *                <groupId>org.springframework.boot</groupId>
 *                 <artifactId>spring-boot-starter-aop</artifactId>
 *             </dependency>
 *          2）、获取当前类真正的代理对象，去调方法即可
 *              1）、@EnableAspectJAutoProxy(exposeProxy = true)：暴露代理对象
 *              2）、获取代理对象
 */
@EnableAspectJAutoProxy(exposeProxy = true)//开启切面自动代理的功能
@EnableTransactionManagement
@EnableDubbo
@MapperScan(basePackages = "com.lq.gmall.pms.mapper")
@SpringBootApplication
public class GamllPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamllPmsApplication.class, args);
    }

}
