package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.BlogComments;
import com.hmdp.service.IBlogCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {
    @Autowired
    private IBlogCommentsService blogCommentsService;

    /**
     * 发表评论（支持二级评论和@回复）
     */
    @PostMapping
    public Result addComment(@RequestBody BlogComments comment) {
        // parentId: 父评论id，answerId: 被回复的评论id
        return blogCommentsService.addComment(comment);
    }

    /**
     * 查询评论列表（支持分页和二级评论）
     */
    @GetMapping("/list")
    public Result listComments(@RequestParam Long blogId,
                              @RequestParam(required = false, defaultValue = "0") Long parentId,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        return blogCommentsService.listComments(blogId, parentId, page, size);
    }

    /**
     * 点赞评论
     */
    @PutMapping("/like/{id}")
    public Result likeComment(@PathVariable Long id) {
        return blogCommentsService.likeComment(id);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id) {
        return blogCommentsService.deleteComment(id);
    }
}
