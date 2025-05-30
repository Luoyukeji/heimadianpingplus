package com.hmdp.service.impl;

import com.hmdp.entity.BlogComments;
import com.hmdp.mapper.BlogCommentsMapper;
import com.hmdp.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.List;
import java.util.Objects;
import com.hmdp.dto.Result;
import com.hmdp.service.IUserService;
import com.hmdp.entity.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IUserService userService;

    @Override
    public Result addComment(BlogComments comment) {
        if (comment.getBlogId() == null || comment.getUserId() == null || comment.getContent() == null) {
            return Result.fail("参数不完整");
        }
        // parentId: 父评论id，answerId: 被回复的评论id
        if (comment.getParentId() == null) comment.setParentId(0L);
        if (comment.getAnswerId() == null) comment.setAnswerId(0L);
        comment.setStatus(false);
        boolean saved = save(comment);
        return saved ? Result.ok() : Result.fail("评论失败");
    }

    @Override
    public Result listComments(Long blogId, Long parentId, Integer page, Integer size) {
        if (blogId == null) {
            return Result.fail("blogId不能为空");
        }
        Page<BlogComments> pageObj = lambdaQuery()
                .eq(BlogComments::getBlogId, blogId)
                .eq(BlogComments::getParentId, parentId == null ? 0 : parentId)
                .eq(BlogComments::getStatus, false)
                .orderByAsc(BlogComments::getCreateTime)
                .page(new Page<>(page, size));
        List<BlogComments> comments = pageObj.getRecords();
        // 封装评论+用户信息
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (BlogComments comment : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("userId", comment.getUserId());
            map.put("blogId", comment.getBlogId());
            map.put("parentId", comment.getParentId());
            map.put("answerId", comment.getAnswerId());
            map.put("content", comment.getContent());
            map.put("liked", comment.getLiked());
            map.put("status", comment.getStatus());
            map.put("createTime", comment.getCreateTime());
            // 查询用户信息
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                map.put("userName", user.getNickName());
                map.put("userIcon", user.getIcon());
            } else {
                map.put("userName", "未知用户");
                map.put("userIcon", null);
            }
            result.add(map);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", result);
        resp.put("total", pageObj.getTotal());
        return Result.ok(resp);
    }

    @Override
    public Result likeComment(Long id) {
        if (id == null) {
            return Result.fail("评论id不能为空");
        }
        BlogComments comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        // 点赞数+1
        comment.setLiked((comment.getLiked() == null ? 0 : comment.getLiked()) + 1);
        boolean updated = updateById(comment);
        return updated ? Result.ok() : Result.fail("点赞失败");
    }

    @Override
    public Result deleteComment(Long id) {
        if (id == null) {
            return Result.fail("评论id不能为空");
        }
        BlogComments comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        // 逻辑删除
        comment.setStatus(true);
        boolean updated = updateById(comment);
        return updated ? Result.ok() : Result.fail("删除失败");
    }
}
