package com.lq.gmall.ums.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.lq.gmall.ums.entity.Member;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.ums.entity.MemberReceiveAddress;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /**
     * 查询会员的所有收货地址
     * @param id
     * @return
     */
    List<MemberReceiveAddress> getMemberAddress(Long id);

    MemberReceiveAddress getMemberAddressByAddressId(Long addressId);
}
