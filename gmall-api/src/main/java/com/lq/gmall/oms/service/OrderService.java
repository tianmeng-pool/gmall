package com.lq.gmall.oms.service;

import com.lq.gmall.oms.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.vo.order.ConfirmOrderVo;
import com.lq.gmall.vo.order.OrderCreateVo;

import java.math.BigDecimal;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface OrderService extends IService<Order> {

    ConfirmOrderVo orderConfirm(Long id);

    OrderCreateVo createOrder(BigDecimal totalPrice, Long addressId, String note);
}
