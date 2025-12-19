package com.zjgsu.lll.secondhand.service;

import com.zjgsu.lll.secondhand.entity.Comment;
import com.zjgsu.lll.secondhand.entity.Order;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final OrderService orderService;

    public CommentService(CommentRepository commentRepository, OrderService orderService) {
        this.commentRepository = commentRepository;
        this.orderService = orderService;
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
        Order order = orderService.getOrderById(comment.getOrderId());

        if (order.getStatus() != 3) {
            throw new BusinessException("Order is not finished, cannot comment");
        }

        if (!order.getBuyerId().equals(comment.getUserId())) {
            throw new BusinessException("Only buyer can comment");
        }

        if (comment.getRating() < 1 || comment.getRating() > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }

        comment.setProductId(order.getProductId());

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
