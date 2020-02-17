package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.pms.entity.ProductAttribute;
import com.lq.gmall.pms.mapper.ProductAttributeMapper;
import com.lq.gmall.pms.service.ProductAttributeService;
import com.lq.gmall.vo.PageInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 商品属性参数表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Component
@Service
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeMapper, ProductAttribute> implements ProductAttributeService {

    @Autowired(required = false)
    private ProductAttributeMapper mapper;

    @Override
    public PageInfoVo getProductAttributes(Long cid, Integer type, Integer pageNum, Integer pageSize) {

        QueryWrapper wrapper = new QueryWrapper<ProductAttribute>().eq("product_attribute_category_id",cid)
                .eq("type",type);

        IPage<ProductAttribute> iPage = mapper.selectPage(new Page<ProductAttribute>(pageSize, pageNum), wrapper);

        return PageInfoVo.getPageVo(iPage,pageSize);
    }
}
