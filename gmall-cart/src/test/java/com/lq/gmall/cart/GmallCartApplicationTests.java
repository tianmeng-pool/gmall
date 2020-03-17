package com.lq.gmall.cart;

import com.alibaba.fastjson.JSON;
import com.lq.gmall.cart.vo.CartItem;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GmallCartApplicationTests {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void contextLoads() {

        RMap<String, Object> map = redissonClient.getMap("cart");
        CartItem cart = new CartItem();
        cart.setSkuId(1L);
        cart.setPrice(new BigDecimal("12.83"));
        cart.setCount(2);

        map.put("1",JSON.toJSONString(cart));
    }

    @Test
    public void test02(){
        RMap<String, Object> map = redissonClient.getMap("cart");

        Object o = map.get("1");
        System.out.println(o);
    }

}
