package com.productservice.repository.cart;

import com.productservice.domain.cart.Cart;
import com.productservice.domain.cart.CartItem;
import com.productservice.domain.product.ProductOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProductOption(Cart cart, ProductOption productOption);

    //productGroup fetch join 이 안됨 //이유를 모르겠음
    @EntityGraph(attributePaths = { "productOption.product.productGroup"})
    List<CartItem> findByCartId(Long cartId);

    //JPQL fetch join
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.productOption po " +
            "JOIN FETCH po.product p " +
            "JOIN FETCH p.productGroup pg " +
            "WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithProductGroup(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

}
