package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);
    Page<Review> findByProductId(Integer productId, Pageable pageable);
    Page<Review> findByProductIdAndRating(Integer productId, Integer rating, Pageable pageable);
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
    boolean existsByOrderDetail(Orderdetail orderDetail);
    List<Review> findAllByOrderByCreatedAtDesc();
    long countByAdminReplyIsNullOrAdminReply(String adminReply);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    double averageRating();


}
