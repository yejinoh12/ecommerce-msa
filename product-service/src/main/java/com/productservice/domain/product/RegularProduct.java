package com.productservice.domain.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("REGULAR")
public class RegularProduct extends Product {

    @Builder
    public RegularProduct(String productName, int price, int stock) {
        super(productName, price, stock);
    }
}
