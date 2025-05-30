package com.hmdp.service;

import com.hmdp.entity.BlogComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogCommentsService extends IService<BlogComments> {
    /**
     * 发表评论（支持二级评论和@回复）
     */
    Result addComment(BlogComments comment);

    /**
     * 查询评论列表（支持分页和二级评论）
     */
    Result listComments(Long blogId, Long parentId, Integer page, Integer size);

    /**
     * 点赞评论
     */
    Result likeComment(Long id);

    /**
     * 删除评论
     */
    Result deleteComment(Long id);
}
