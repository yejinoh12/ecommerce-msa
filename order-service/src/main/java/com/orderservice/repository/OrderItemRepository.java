package com.orderservice.repository;


import com.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


    @Query("SELECT oi.id FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<Long> findOrderItemIdsByOrderId(@Param("orderId") Long orderId);

    List<OrderItem> findByOrderId(Long OrderId);
}
