package com.productservice.repository;

import com.productservice.entity.Like;
import com.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository  extends JpaRepository<Like, Long> {

    // 특정 유저가 특정 제품에 좋아요를 눌렀는지 확인
    Optional<Like> findByUserIdAndProduct(Long userId, Product product);
}
