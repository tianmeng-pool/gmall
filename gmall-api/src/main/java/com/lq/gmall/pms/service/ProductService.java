package com.lq.gmall.pms.service;

import com.lq.gmall.pms.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.vo.PageInfoVo;
import com.lq.gmall.vo.product.PmsProductQueryParam;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface ProductService extends IService<Product> {

    PageInfoVo getPageProduct(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum);
}
