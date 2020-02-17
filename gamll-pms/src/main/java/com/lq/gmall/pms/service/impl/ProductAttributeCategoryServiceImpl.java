package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.pms.entity.ProductAttributeCategory;
import com.lq.gmall.pms.mapper.ProductAttributeCategoryMapper;
import com.lq.gmall.pms.service.ProductAttributeCategoryService;
import com.lq.gmall.vo.PageInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 产品属性分类表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Component
@Service
public class ProductAttributeCategoryServiceImpl extends ServiceImpl<ProductAttributeCategoryMapper, ProductAttributeCategory> implements ProductAttributeCategoryService {

    @Autowired(required = false)
    private ProductAttributeCategoryMapper mapper;

    @Override
    public PageInfoVo productAttributePageInfo(Integer pageNum, Integer pageSize) {

        IPage<ProductAttributeCategory> iPage = mapper.selectPage(new Page<ProductAttributeCategory>(pageNum, pageSize),null);

        return PageInfoVo.getPageVo(iPage,pageSize);
    }
}
