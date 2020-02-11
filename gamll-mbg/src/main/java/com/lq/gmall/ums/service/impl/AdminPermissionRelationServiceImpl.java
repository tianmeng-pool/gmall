package com.lq.gmall.ums.service.impl;

import com.lq.gmall.ums.entity.AdminPermissionRelation;
import com.lq.gmall.ums.mapper.AdminPermissionRelationMapper;
import com.lq.gmall.ums.service.AdminPermissionRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 后台用户和权限关系表(除角色中定义的权限以外的加减权限) 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class AdminPermissionRelationServiceImpl extends ServiceImpl<AdminPermissionRelationMapper, AdminPermissionRelation> implements AdminPermissionRelationService {

}
