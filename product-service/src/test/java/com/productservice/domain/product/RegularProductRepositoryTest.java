package com.productservice.domain.product;


import com.productservice.repository.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RegularProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void RegularProductSaveTest() {

        // Given
        RegularProduct regularProduct = RegularProduct.builder()
                .productName("Regular Product")
                .price(100)
                .stock(50)
                .build();

        // When
        RegularProduct savedProduct = productRepository.save(regularProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductName()).isEqualTo("Regular Product");
        assertThat(savedProduct.getPrice()).isEqualTo(100);
        assertThat(savedProduct.getStock()).isEqualTo(50);
    }
}