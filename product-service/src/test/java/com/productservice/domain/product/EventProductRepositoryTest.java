//package com.productservice.domain.product;
//
//import com.productservice.repository.ProductRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Transactional
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class EventProductRepositoryTest {
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Test
//    public void LimitedProductSaveTest() {
//
//        // Given
//        LocalDateTime eventStartTime = LocalDateTime.now().minusDays(1);
//        LocalDateTime eventEndTime = LocalDateTime.now().plusDays(1);
//
//        EventProduct eventProduct = new EventProduct(
//                "Limited Edition Product",
//                150,
//                20,
//                eventStartTime,
//                eventEndTime
//        );
//
//        // When
//        EventProduct savedProduct = productRepository.save(eventProduct);
//
//        // Then
//        assertThat(savedProduct).isNotNull();
//        assertThat(savedProduct.getId()).isNotNull();
//        assertThat(savedProduct.getProductName()).isEqualTo("Limited Edition Product");
//        assertThat(savedProduct.getPrice()).isEqualTo(150);
//        assertThat(savedProduct.getStock()).isEqualTo(20);
//        assertThat(savedProduct.getEventStartTime()).isEqualTo(eventStartTime);
//        assertThat(savedProduct.getEventEndTime()).isEqualTo(eventEndTime);
//    }
//}
