package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.pms.entity.Product;
import com.lq.gmall.pms.mapper.ProductMapper;
import com.lq.gmall.pms.service.ProductService;
import com.lq.gmall.vo.PageInfoVo;
import com.lq.gmall.vo.product.PmsProductQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
@Component
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired(required = false)
    private ProductMapper mapper;

    /**
     * 分页查询商品数据
     * @param productQueryParam
     * @param pageSize
     * @param pageNum
     * @return
     */
    @Override
    public PageInfoVo getPageProduct(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {

        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if (productQueryParam.getPublishStatus() != null) {
            wrapper.eq("publish_status",productQueryParam.getPublishStatus());
        }

        if (productQueryParam.getVerifyStatus() != null) {
            wrapper.eq("verify_status",productQueryParam.getVerifyStatus());
        }

        if (!StringUtils.isEmpty(productQueryParam.getKeyword())) {
            wrapper.like("name",productQueryParam.getKeyword());
        }

        if (!StringUtils.isEmpty(productQueryParam.getProductSn())) {
            wrapper.like("product_sn",productQueryParam.getProductSn());
        }

        if (productQueryParam.getProductCategoryId() != null) {
            wrapper.eq("product_category_id",productQueryParam.getProductCategoryId());
        }

        if (productQueryParam.getBrandId() != null) {
            wrapper.eq("brand_id",productQueryParam.getBrandId());
        }

        IPage<Product> productIPage = mapper.selectPage(new Page<Product>(Integer.valueOf(pageNum).longValue(), Integer.valueOf(pageSize).longValue()), wrapper);

        return new PageInfoVo(productIPage.getTotal(),productIPage.getPages(),Integer.valueOf(pageNum).longValue(), Integer.valueOf(pageSize).longValue(),productIPage.getRecords());
    }
}
