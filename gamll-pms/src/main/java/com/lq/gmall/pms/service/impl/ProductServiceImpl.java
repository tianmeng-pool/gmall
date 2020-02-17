package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.pms.entity.*;
import com.lq.gmall.pms.mapper.*;
import com.lq.gmall.pms.service.ProductService;
import com.lq.gmall.vo.PageInfoVo;
import com.lq.gmall.vo.product.PmsProductParam;
import com.lq.gmall.vo.product.PmsProductQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired(required = false)
    private ProductMapper productMapper;

    @Autowired(required = false)
    private ProductAttributeValueMapper productAttributeValueMapper;

    @Autowired(required = false)
    private ProductFullReductionMapper productFullReductionMapper;

    @Autowired(required = false)
    private ProductLadderMapper productLadderMapper;

    @Autowired(required = false)
    private SkuStockMapper skuStockMapper;

    /**当前线程共享同样的数据*/
    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //ThreadLocal底层原理
    private Map<Thread,Long> map = new HashMap<>();

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

        IPage<Product> productIPage = productMapper.selectPage(new Page<Product>(Integer.valueOf(pageNum).longValue(), Integer.valueOf(pageSize).longValue()), wrapper);

        return new PageInfoVo(productIPage.getTotal(),productIPage.getPages(),Integer.valueOf(pageNum).longValue(), Integer.valueOf(pageSize).longValue(),productIPage.getRecords());
    }

    /**
     * 保存商品信息
     * @param productParam
     *
     * 同一次调用，只要上面方法的数据，下面方法要用，我们就可以使用ThreadLocal共享数据
     *
     * 考虑事务：
     *  1.哪些东西是一定要回滚的、哪些即使出错了也不必要回滚的。
     *     商品的核心信息(商品的基本数据、sku)保存的时候，不要受到别的无关信息的影响
     *     无关信息出问题、核心信息也不用回滚的
     *  2.事务的传播行为:propagation:当前方法的事务[是否要和别人共用一个事务]如何传播下去(里面的方法如果用事务，是否和它共用一个事务)
     *
     *     Propagation propagation() default Propagation.REQUIRED;
     *
     *     REQUIRED:(必须)
     *         如果以前有事务，就和之前的事务共用一个事务，没有就创建一个事务
     *     SUPPORTS:(支持的)
     *          之前有事务，就以事务的方式运作，没有事务也可以
     *     MANDATORY:(强制)
     *          一定要有事务，没有事务就报错
     *     REQUIRES_NEW:(总是用新的事务)
     *          创建一个新的事务，如果以前有事务，就暂停前面的事务
     *     NOT_SUPPORTED:(不支持的)
     *          不支持事务内运作，如果已经有事务了，就挂起当前存在的事务
     *     NEVER:(从不使用)
     *          不支持事务内运作，如果已经有事务了，抛异常
     *     NESTED:
     *          开启一个子事务(mysql不支持)，需要支持还原点功能的数据库才行
     *
     *
     *  隔离级别
     *
     *
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveProduct(PmsProductParam productParam) {
        //1.pms_product：保存商品基本信息
       saveBaseProduct(productParam);

        //2.pms_product_attribute_value：保存这个商品所对应的所有属性的值
        saveProductAttributeValue(productParam);

        //3.pms_product_full_reduction：保存商品的满减信息
        saveProductFullReduction(productParam);

        //4.pms_product_ladder：爆粗商品的阶梯信息
        saveLadder(productParam);

        //5.pms_sku_stock：sku库存表
        saveSkuStock(productParam);
       /* skuStockList.forEach((skuStock) -> {
            skuStock.setProductId(product.getId());
            skuStockMapper.insert(skuStock);
        });*/

    }

    @Transactional
    public void saveSkuStock(PmsProductParam productParam) {
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        for (int i = 0; i < skuStockList.size(); i++) {
            SkuStock skuStock = skuStockList.get(i);
            //必须要有skuCode
            if (StringUtils.isEmpty(skuStock.getSkuCode())) {
                //skuCode:1_1 1_2 1_3 1_4
                //skuCode生成规则：商品id_sku自增id
                skuStock.setSkuCode(threadLocal.get() + "_" + i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional
    public void saveLadder(PmsProductParam productParam) {
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((productLadder) -> {
            productLadder.setProductId(threadLocal.get());
            productLadderMapper.insert(productLadder);
        });
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional
    public void saveProductFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> productFullReductionList = productParam.getProductFullReductionList();
        productFullReductionList.forEach((productFullReduction) -> {
            productFullReduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(productFullReduction);
        });
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional
    public void saveProductAttributeValue(PmsProductParam productParam) {
        List<ProductAttributeValue> productAttributeValueList = productParam.getProductAttributeValueList();
        productAttributeValueList.forEach((productAttributeValue) -> {
            //设置商品的id
            productAttributeValue.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(productAttributeValue);
        });
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional
    public void saveBaseProduct(PmsProductParam productParam) {
        Product product = new Product();
        BeanUtils.copyProperties(productParam,product);
        productMapper.insert(product);
        //mybatis-plus能自动获取到刚才这个数据的自增id
        log.debug("保存的商品id:{}",product.getId());
        //把商品id存入ThreadLocal中
        threadLocal.set(product.getId());
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }
}
