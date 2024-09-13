package com.common.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockResponse {
    private Long productId;
    private int stock;
    private LocalDateTime start;
    private LocalDateTime end;

    public StockResponse(Long productId, int stock) {
        this.productId = productId;
        this.stock = stock;
    }
}
