package com.lq.gmall.pms.service;

import com.lq.gmall.pms.entity.ProductAttribute;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.vo.PageInfoVo;

/**
 * <p>
 * 商品属性参数表 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface ProductAttributeService extends IService<ProductAttribute> {
    /**
     * 根据商品属性分类查询该分类下的销售属性和基本规格参数
     * @param cid
     * @param type
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfoVo getProductAttributes(Long cid, Integer type, Integer pageNum, Integer pageSize);
}
