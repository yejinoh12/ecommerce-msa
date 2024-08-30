package com.productservice.domain.product;

import com.common.exception.BaseBizException;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**********************************************************
 * 싱글 테이블 전략을 사용해서 일반 상품과 이벤트 상품을 구분
 **********************************************************/

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorColumn(name = "dtype")
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int price;

    @Setter
    @Column(nullable = false)
    private int stock;

    public boolean canPurchase(int quantity) {
        return stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new BaseBizException("재고가 부족으로 주문에 실패했습니다.");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public Product(Long id, String productName, int price, int stock) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    public Product(String productName, int price, int stock) {
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

}
