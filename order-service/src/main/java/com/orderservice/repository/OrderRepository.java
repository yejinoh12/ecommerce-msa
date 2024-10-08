package com.orderservice.repository;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.deliveryStatus = 'SHIPPED', o.modifiedAt = :modifiedAt " +
            "WHERE o.deliveryStatus = 'PENDING' AND o.createdAt <= :date")
    void updateDeliveryStatusShipped(@Param("date") LocalDateTime date, @Param("modifiedAt") LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.deliveryStatus = 'DELIVERED', o.modifiedAt = :modifiedAt " +
            "WHERE o.deliveryStatus = 'SHIPPED' AND o.modifiedAt <= :date")
    void updateDeliveryStatusDelivered(@Param("date") LocalDateTime date, @Param("modifiedAt") LocalDateTime modifiedAt);

}
