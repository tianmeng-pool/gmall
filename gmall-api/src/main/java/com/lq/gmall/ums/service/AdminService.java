package com.lq.gmall.ums.service;

import com.lq.gmall.ums.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 后台用户表 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface AdminService extends IService<Admin> {

    Admin login(String username, String password);

    Admin getUserInfo(String userName);
}
