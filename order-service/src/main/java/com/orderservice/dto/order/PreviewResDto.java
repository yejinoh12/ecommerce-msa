package com.orderservice.dto.order;

import com.common.dto.user.AddressResDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreviewResDto {
    OrderItemDto item;
    AddressResDto address;
}
