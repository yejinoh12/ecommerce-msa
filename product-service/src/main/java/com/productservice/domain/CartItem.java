package com.productservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private Product product;

    @Column(nullable = false)
    private int count;


    @Builder
    public CartItem(Cart cart, Product product, int count) {
        this.cart = cart;
        this.product = product;
        this.count = count;
    }

    public void addCount(int cnt) {
        this.count += cnt;
    }

    public void subCount(int cnt) {
        this.count -= cnt;
    }

}
