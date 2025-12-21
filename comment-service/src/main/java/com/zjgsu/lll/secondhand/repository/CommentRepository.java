package com.zjgsu.lll.secondhand.repository;

import com.zjgsu.lll.secondhand.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByProductId(Long productId);

    List<Comment> findByUserId(Long userId);

    List<Comment> findByOrderId(Long orderId);
}
