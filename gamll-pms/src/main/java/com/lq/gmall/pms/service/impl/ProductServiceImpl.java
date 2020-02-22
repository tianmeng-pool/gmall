package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.constant.EsConstant;
import com.lq.gmall.pms.entity.*;
import com.lq.gmall.pms.mapper.*;
import com.lq.gmall.pms.service.ProductService;
import com.lq.gmall.to.es.EsProduct;
import com.lq.gmall.to.es.EsProductAttributeValue;
import com.lq.gmall.to.es.EsSkuProductInfo;
import com.lq.gmall.vo.PageInfoVo;
import com.lq.gmall.vo.product.PmsProductParam;
import com.lq.gmall.vo.product.PmsProductQueryParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
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
 *
 * 查询要多试几次，增删改要快速失败
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

    @Autowired
    private JestClient  jestClient;

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
     *  总结：传播行为过程中，只要Requires_new被执行过就一定成功，不管后面出不出问题，
     *       异常机制还是一样的，出现异常代码以后不执行，Required只要感知到异常就一定回滚，和外事务是什么传播行为无关
     *  传播行为总是来定义，当一个事务存在的时候，他内部的事务该怎么执行。
     *
     *  事务的问题:
     *      Service自己调用自己的方法，无法加上真正的自己内部调整的各个事务
     *      解决： 如果是对象.方法()就可以
     *          1.要是能拿到ioc容器，从容器中再把我们的组件获取一下，用对象调方法
     *
     *  隔离级别: 解决读写加锁问题的(数据库底层方案) mysql默认可重复度(快照读)
     *
     *  读未提交
     *  读已提交
     *  可重复度
     *  串行化
     *
     *  异常回滚策略:
     *  异常：
     *      运行时异常(不受检查异常)
     *      编译时异常(受检异常)
     *
     *  运行时异常默认是回滚的
     *  编译时异常默认是不会滚的
     *      rollbackFor:指定哪些异常需要回滚
     *      noRollbackFor:指定哪些异常不需要回滚
     *
     *
     */
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public void saveProduct(PmsProductParam productParam) {
        //获取当前类的代理对象
        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();
        log.debug("当前类的代理对象为:{}",proxy);
        //1.pms_product：保存商品基本信息
        proxy.saveBaseProduct(productParam);

        //2.pms_product_attribute_value：保存这个商品所对应的所有属性的值
        proxy.saveProductAttributeValue(productParam);

        //3.pms_product_full_reduction：保存商品的满减信息
        proxy.saveProductFullReduction(productParam);

        //4.pms_product_ladder：爆粗商品的阶梯信息
        proxy.saveLadder(productParam);

        //5.pms_sku_stock：sku库存表
        proxy.saveSkuStock(productParam);
       /* skuStockList.forEach((skuStock) -> {
            skuStock.setProductId(product.getId());
            skuStockMapper.insert(skuStock);
        });*/

    }

    @Override
    public Product productInfo(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public void updatePublishStatus(List<Long> ids, Integer publishStatus) {
        if (publishStatus == 0) {
           ids.forEach((id) -> {
               //商品下架
               //改数据库状态
               updateProductPublishStatus(publishStatus, id);
               //删es
               deleteProductFromEs(id);
           });
        } else {
            //商品上架
            ids.forEach((id) -> {
                //改数据库状态
                //javaBean应该都用包装类型,修改数据库状态
                updateProductPublishStatus(publishStatus, id);
                //存es
                saveProductToEs(id);
            });
        }

    }

    private void deleteProductFromEs(Long id) {
        //从es中删除商品数据
        try {
            Delete build = new Delete.Builder(id.toString())
                    .index(EsConstant.ES_PRODUCT_INDEX)
                    .type(EsConstant.ES_PRODUCT_INDEX_INFO)
                    .build();

            DocumentResult execute = jestClient.execute(build);
            if (execute.isSucceeded()) {
                log.info("在es中删除id为{}号的商品成功",id);
            } else {
                log.error("在es中删除商品为{}号的商品失败",id);
            }
        } catch (IOException e) {
            log.error("在es中删除商品为{}号的商品失败,错误信息为:{}",id,e.getMessage());
        }

    }

    /**
     * 给数据库插入数据
     *  1.dubbo远程调用插入数据服务，可能经常超时。dubbo默认会重试
     *      导致这个方法会被调用多次。可能导致数据库同样的数据有多个
     *
     *  2.dubbo有自己默认的集群容错
     *
     *  给数据库插入数据的，最好用dubbo的快速失败模式。我们手工重试
     * @param id
     */
    public void saveProductToEs(Long id) {
        //1.查出商品的基本信息
        Product productInfo = productInfo(id);

        EsProduct esProduct = new EsProduct();

        //1.复制基本信息
        BeanUtils.copyProperties(productInfo,esProduct);

        //2.复制sku信息,对于es要保存商品信息，还要查出这个商品的sku，给es中保存
        List<SkuStock> skuStocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuProductInfo> esSkuProductInfos = new ArrayList<>(skuStocks.size());

        //查出当前的sku属性
        List<ProductAttribute> skuAttrNames =  productAttributeValueMapper.selectProductSaveAttrName(id);
        skuStocks.forEach((skuStock) -> {
            EsSkuProductInfo esSkuProductInfo = new EsSkuProductInfo();

            BeanUtils.copyProperties(skuStock,esSkuProductInfo);

            //设置sku的特有标题信息:商品属性名+sku信息
            String subTitle = esProduct.getName();
            if (!StringUtils.isEmpty(skuStock.getSp1())) {
                subTitle += " "+skuStock.getSp1();
            }
            if (!StringUtils.isEmpty(skuStock.getSp2())) {
                subTitle += " "+skuStock.getSp2();
            }
            if (!StringUtils.isEmpty(skuStock.getSp3())) {
                subTitle += " "+skuStock.getSp3();
            }
            esSkuProductInfo.setSkuTitle(subTitle);

            List<EsProductAttributeValue> attributeValues = new ArrayList<>();
            for (int i = 0; i < skuAttrNames.size(); i++) {
                EsProductAttributeValue value = new EsProductAttributeValue();

                value.setName(skuAttrNames.get(i).getName());
                value.setProductAttributeId(skuAttrNames.get(i).getId());
                value.setType(skuAttrNames.get(i).getType());
                value.setProductId(id);

                if (i == 0) {
                    value.setValue(skuStock.getSp1());
                }
                if (i == 1) {
                    value.setValue(skuStock.getSp2());
                }
                if (i == 2) {
                    value.setValue(skuStock.getSp3());
                }
                attributeValues.add(value);

            }

            //设置sku的属性值
            esSkuProductInfo.setAttributeValues(attributeValues);

            esSkuProductInfos.add(esSkuProductInfo);

        });

        //查询这个sku所有销售属性对应的值.要统计数据库中这个sk有多少
        esProduct.setEsSkuProductInfos(esSkuProductInfos);

        /**
         * ##查出某个商品所对应的销售属性以及它的值
         * select pav.*,pa.name,pa.type from pms_product_attribute_value pav
         * LEFT JOIN pms_product_attribute pa on pa.id=pav.product_attribute_id
         * where pav.product_id=23 and pa.type=0
         */
        //3.复制公共属性信息，查出这个商品的公共属性
        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductBaseAttrAndValue(id);
        esProduct.setAttrValueList(attributeValues);

        //把商品数据保存入es中
        try {
            Index build = new Index.Builder(esProduct)
                    .index(EsConstant.ES_PRODUCT_INDEX)
                    .type(EsConstant.ES_PRODUCT_INDEX_INFO)
                    .id(id.toString())
                    .build();
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if (succeeded) {
                log.info("商品id为{}号的商品在es中保存成功",id);
            } else {
                log.error("商品id为{}号的商品在es中保存失败",id);
            }
        } catch (IOException e) {
            log.error("商品在es中保存失败,商品id为{},错误信息为:{}",id,e.getMessage());
        }
    }

    public void updateProductPublishStatus(Integer publishStatus, Long id) {
        Product product = new Product();
        //默认所有属性为null
        product.setId(id);
        product.setPublishStatus(publishStatus);
        //mybatis-plus 自带的更新方法是哪个字段有值就更新哪个字段
        productMapper.updateById(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public void saveLadder(PmsProductParam productParam) {
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((productLadder) -> {
            productLadder.setProductId(threadLocal.get());
            productLadderMapper.insert(productLadder);
        });
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public void saveProductFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> productFullReductionList = productParam.getProductFullReductionList();
        productFullReductionList.forEach((productFullReduction) -> {
            productFullReduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(productFullReduction);
        });
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public void saveProductAttributeValue(PmsProductParam productParam) {
        List<ProductAttributeValue> productAttributeValueList = productParam.getProductAttributeValueList();
        productAttributeValueList.forEach((productAttributeValue) -> {
            //设置商品的id
            productAttributeValue.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(productAttributeValue);
        });
        log.debug("当前线程id,name:{},{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
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
