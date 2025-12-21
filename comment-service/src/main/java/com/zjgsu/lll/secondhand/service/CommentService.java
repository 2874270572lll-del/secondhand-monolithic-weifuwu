package com.zjgsu.lll.secondhand.service;

import com.zjgsu.lll.secondhand.client.OrderClient;
import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Comment;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final OrderClient orderClient;

    public CommentService(CommentRepository commentRepository, OrderClient orderClient) {
        this.commentRepository = commentRepository;
        this.orderClient = orderClient;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Comment not found"));
    }

    public List<Comment> getCommentsByProduct(Long productId) {
        return commentRepository.findByProductId(productId);
    }

    public List<Comment> getCommentsByUser(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    public List<Comment> getCommentsByOrder(Long orderId) {
        return commentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Comment createComment(Comment comment) {
        // Call order service to validate order
        Result<?> orderResult = orderClient.getOrderById(comment.getOrderId());

        if (orderResult.getCode() != 200 || orderResult.getData() == null) {
            throw new BusinessException("Order not found");
        }

        // Note: In a real implementation, you would parse the order data
        // and validate the order status and buyer

        if (comment.getRating() < 1 || comment.getRating() > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new BusinessException("Comment not found");
        }
        commentRepository.deleteById(id);
    }
}
