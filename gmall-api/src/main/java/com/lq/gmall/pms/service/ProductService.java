package com.lq.gmall.pms.service;

import com.lq.gmall.pms.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.vo.PageInfoVo;
import com.lq.gmall.vo.product.PmsProductParam;
import com.lq.gmall.vo.product.PmsProductQueryParam;

import java.util.List;

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

    /**
     * 保存商品信息
     * @param productParam
     */
    void saveProduct(PmsProductParam productParam);

    /**
     * 批量上下架
     * @param ids
     * @param publishStatus
     */
    void updatePublishStatus(List<Long> ids, Integer publishStatus);

    /**
     * 根据商品id查询商品的详情信息
     * @param id
     * @return
     */
    Product productInfo(Long id);
}
