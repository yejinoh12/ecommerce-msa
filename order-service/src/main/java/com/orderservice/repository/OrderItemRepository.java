package com.orderservice.repository;

import com.common.dto.order.IncreaseStockReqDto;
import com.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long OrderId);

    //특정 주문에 대한 DTO 생성
    @Query("SELECT new com.common.dto.order.IncreaseStockReqDto(oi.productId, oi.quantity) " +
            "FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<IncreaseStockReqDto> findOrderItemDtosByOrderId(@Param("orderId") Long orderId);

    //여러 주문에 대한 DTO 생성
    @Query("SELECT new com.common.dto.order.IncreaseStockReqDto(oi.productId, oi.quantity) " +
            "FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    List<IncreaseStockReqDto> findOrderItemDtosByOrderIds(@Param("orderIds") List<Long> orderIds);
}
