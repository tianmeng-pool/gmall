package com.lq.gmall.oms.service.impl;

import com.lq.gmall.oms.entity.CartItem;
import com.lq.gmall.oms.mapper.CartItemMapper;
import com.lq.gmall.oms.service.CartItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

}
