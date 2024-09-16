package com.common.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResDto {
    private Long addressId;
    private String alias;
    private String address;
    private String detailAddress;
    private String phone;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
