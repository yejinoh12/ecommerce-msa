package com.orderservice.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderReqDto {
    private Long productId;
    private String name;
    private int unitPrice;
    private int cnt;
    private String addrAlias;
    private String address;
    private String addrDetail;
    private String phone;
}
