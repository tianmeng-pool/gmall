package com.lq.gmall.pms;

import com.lq.gmall.pms.entity.Product;
import com.lq.gmall.pms.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GamllPmsApplicationTests {

    @Autowired
    private ProductService productService;

    @Test
    void contextLoads() {

        Product product = productService.getById(1);
        System.out.println(product);

    }

}
