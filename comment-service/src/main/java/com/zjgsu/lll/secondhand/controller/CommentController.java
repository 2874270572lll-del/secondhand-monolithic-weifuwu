package com.zjgsu.lll.secondhand.controller;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Comment;
import com.zjgsu.lll.secondhand.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<List<Comment>> getAllComments() {
        return Result.success(commentService.getAllComments());
    }

    @GetMapping("/{id}")
    public Result<Comment> getCommentById(@PathVariable Long id) {
        return Result.success(commentService.getCommentById(id));
    }

    @GetMapping("/product/{productId}")
    public Result<List<Comment>> getCommentsByProduct(@PathVariable Long productId) {
        return Result.success(commentService.getCommentsByProduct(productId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Comment>> getCommentsByUser(@PathVariable Long userId) {
        return Result.success(commentService.getCommentsByUser(userId));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<Comment>> getCommentsByOrder(@PathVariable Long orderId) {
        return Result.success(commentService.getCommentsByOrder(orderId));
    }

    @PostMapping
    public Result<Comment> createComment(@Valid @RequestBody Comment comment) {
        return Result.success(commentService.createComment(comment));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
