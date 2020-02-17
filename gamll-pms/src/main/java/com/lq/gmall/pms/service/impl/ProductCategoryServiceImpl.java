package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.constant.SysConstant;
import com.lq.gmall.pms.entity.ProductCategory;
import com.lq.gmall.pms.mapper.ProductCategoryMapper;
import com.lq.gmall.pms.service.ProductCategoryService;
import com.lq.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Slf4j
@Service
@Component
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired(required = false)
    private ProductCategoryMapper mapper;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 分布式缓存用redis来做
     * @param i
     * @return
     */
    @Override
    public List<PmsProductCategoryWithChildrenItem> listCatelogChilder(int i) {
        List<PmsProductCategoryWithChildrenItem> items = null;
        Object sys_menu = redisTemplate.opsForValue().get(SysConstant.Category_Sys_Cache_Menu);
        if (sys_menu != null) {
            //缓存中有数据
            items = (List<PmsProductCategoryWithChildrenItem>) sys_menu;
            log.debug("数据用缓存中读取...");
        } else {
            //从数据库中查询
            items = mapper.listCatelogChilder(i);
            //将查询出来的菜单数据存入redis
            redisTemplate.opsForValue().set(SysConstant.Category_Sys_Cache_Menu,items);
            log.debug("数据从数据库中读取...");
        }
        return items;
    }
}
