package com.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressAddReqDto {
    private String alias;
    private String postalCode;
    private String address;
    private String detailAddr;
    private String phone;

    @JsonProperty("isDefault")
    private boolean Default;
}