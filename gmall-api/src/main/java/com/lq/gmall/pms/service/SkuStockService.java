package com.lq.gmall.pms.service;

import com.lq.gmall.pms.entity.SkuStock;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * sku的库存 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface SkuStockService extends IService<SkuStock> {

    BigDecimal getSkuPriceBySkuId(Long skuId);
}
