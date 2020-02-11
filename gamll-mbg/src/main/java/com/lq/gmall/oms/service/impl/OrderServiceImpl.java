package com.lq.gmall.oms.service.impl;

import com.lq.gmall.oms.entity.Order;
import com.lq.gmall.oms.mapper.OrderMapper;
import com.lq.gmall.oms.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

}
