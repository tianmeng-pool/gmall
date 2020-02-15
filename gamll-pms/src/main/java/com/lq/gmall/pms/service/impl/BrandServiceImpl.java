package com.lq.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lq.gmall.pms.entity.Brand;
import com.lq.gmall.pms.mapper.BrandMapper;
import com.lq.gmall.pms.service.BrandService;
import com.lq.gmall.vo.PageInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Component
@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Autowired(required = false)
    private BrandMapper mapper;

    @Override
    public PageInfoVo brandPageInfo(String keyword, Integer pageNum, Integer pageSize) {

        QueryWrapper<Brand> wrapper = new QueryWrapper<>();

        if (StringUtils.isEmpty(keyword)) {
            wrapper.like("name",keyword);
        }

        IPage<Brand> iPage = mapper.selectPage(new Page<Brand>(pageNum.longValue(), pageSize.longValue()), wrapper);

        PageInfoVo page = new PageInfoVo(iPage.getTotal(), iPage.getPages(),pageSize.longValue(),pageNum.longValue(), iPage.getRecords());

        return page;
    }
}
