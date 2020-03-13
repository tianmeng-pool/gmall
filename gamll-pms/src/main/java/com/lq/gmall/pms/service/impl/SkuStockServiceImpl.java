package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.pms.entity.SkuStock;
import com.lq.gmall.pms.mapper.SkuStockMapper;
import com.lq.gmall.pms.service.SkuStockService;
import org.springframework.stereotype.Component;

/**
 * <p>
 * sku的库存 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
@Component
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {

}
