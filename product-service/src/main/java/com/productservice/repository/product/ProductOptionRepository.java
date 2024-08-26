package com.productservice.repository.product;

import com.productservice.domain.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductId(Long productId);

    @Query("SELECT po FROM ProductOption po JOIN FETCH po.product p JOIN FETCH p.productGroup WHERE po.id IN :productOptionIds")
    List<ProductOption> findWithProductAndGroupById(@Param("productOptionIds") List<Long> productOptionIds);

}