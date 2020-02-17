package com.lq.gmall.pms;

import com.lq.gmall.pms.entity.Brand;
import com.lq.gmall.pms.entity.Product;
import com.lq.gmall.pms.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GamllPmsApplicationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplateObj;

    @Test
    void contextLoads() {

        Product product = productService.getById(1);
        System.out.println(product);

    }

    @Test
    public void redisTemplate(){
        redisTemplate.opsForValue().set("hello","redis");
        System.out.println("保存数据成功......");
        String hello = redisTemplate.opsForValue().get("hello");
        System.out.println(hello);
    }

    /**
     * redis中存对象是m默认使用序列化的方式，把对象弄过去
     */
    @Test
    public void redisTemplateObj(){
        //以后要存对象将对象转为json字符串
        //去redis中取出来，反序列化为对象

        Brand brand = new Brand();

        brand.setName("手机");

        redisTemplateObj.opsForValue().set("hhh",brand);

        Brand hhh = (Brand) redisTemplateObj.opsForValue().get("hhh");

        System.out.println(hhh.getName());

    }

}
