package com.lq.gmall.pms.service;

import com.lq.gmall.pms.entity.ProductAttributeCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.vo.PageInfoVo;

/**
 * <p>
 * 产品属性分类表 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface ProductAttributeCategoryService extends IService<ProductAttributeCategory> {

    /**
     * 分页获取商品属性分类
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfoVo productAttributePageInfo(Integer pageNum, Integer pageSize);
}
