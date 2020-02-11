package com.lq.gmall.ums.service.impl;

import com.lq.gmall.ums.entity.Member;
import com.lq.gmall.ums.mapper.MemberMapper;
import com.lq.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

}
