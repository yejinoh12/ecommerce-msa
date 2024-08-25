package com.productservice.domain.product;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ProductGroup {

    @Id
    @Column(name = "product_group_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String groupName;

    @Column(nullable = false)
    private int price;

    @OneToMany(mappedBy = "productGroup")
    private List<Product> products;

}
