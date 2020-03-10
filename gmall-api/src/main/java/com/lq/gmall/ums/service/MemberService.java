package com.lq.gmall.ums.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.lq.gmall.ums.entity.Member;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 会员表 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface MemberService extends IService<Member> {

    Member login(String username, String password);
}
