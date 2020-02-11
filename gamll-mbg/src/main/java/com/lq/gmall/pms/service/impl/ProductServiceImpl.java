package com.lq.gmall.pms.service.impl;

import com.lq.gmall.pms.entity.Product;
import com.lq.gmall.pms.mapper.ProductMapper;
import com.lq.gmall.pms.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
