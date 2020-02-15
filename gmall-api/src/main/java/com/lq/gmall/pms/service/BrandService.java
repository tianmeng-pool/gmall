package com.lq.gmall.pms.service;

import com.lq.gmall.pms.entity.Brand;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lq.gmall.vo.PageInfoVo;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
public interface BrandService extends IService<Brand> {

    /**
     * 分页查询品牌数据
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfoVo brandPageInfo(String keyword, Integer pageNum, Integer pageSize);
}
