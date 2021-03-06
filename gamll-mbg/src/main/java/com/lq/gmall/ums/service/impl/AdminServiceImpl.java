package com.lq.gmall.ums.service.impl;

import com.lq.gmall.ums.entity.Admin;
import com.lq.gmall.ums.mapper.AdminMapper;
import com.lq.gmall.ums.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

}
