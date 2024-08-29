package com.productservice.repository.product;

import com.productservice.domain.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    //일반 상품 조회
    @Query("SELECT p FROM Product p WHERE TYPE(p) = RegularProduct")
    List<Product> findRegularProducts();

    //이벤트 상품 조회
    @Query("SELECT p FROM Product p WHERE TYPE(p) = EventProduct")
    List<Product> findEventProducts();

    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(Long id);
}