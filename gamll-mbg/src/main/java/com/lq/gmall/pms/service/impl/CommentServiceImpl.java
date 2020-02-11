package com.lq.gmall.pms.service.impl;

import com.lq.gmall.pms.entity.Comment;
import com.lq.gmall.pms.mapper.CommentMapper;
import com.lq.gmall.pms.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品评价表 服务实现类
 * </p>
 *
 * @author lq
 * @since 2020-02-10
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}
