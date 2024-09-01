package com.productservice.service.stock;

import com.common.dto.order.DecreaseStockReqDto;
import com.common.dto.order.IncreaseStockReqDto;
import com.common.dto.product.StockResponse;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.domain.product.Product;
import com.productservice.dto.product.ProductStock;
import com.productservice.repository.product.ProductRepository;
import com.productservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final RedisStockService redisStockService;
    private final ProductService productService;

    /**********************************************************
     * 주문 서비스 요청 API
     **********************************************************/

    public StockResponse getProductStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
        return new StockResponse(productId, product.getStock());
    }

    /**********************************************************
     * DB 재고 감소 요청(상품 서비스 요청 API)
     **********************************************************/

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.decreaseStock(quantity);
        productRepository.saveAndFlush(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.increaseStock(quantity);
        productRepository.saveAndFlush(product);
    }

    /**********************************************************
     * 미사용 트랜잭션
     **********************************************************/

    @Transactional
    public void decreaseStock(List<DecreaseStockReqDto> decreaseStockReqDtos){
        log.info("재고 감소 로직 시작");
        for (DecreaseStockReqDto dto : decreaseStockReqDtos) {
            Product product = productRepository.findByIdWithPessimisticLock(dto.getProductId())
                    .orElseThrow(() -> new BaseBizException("productID가 " + dto.getProductId() + "인 상품 옵션을 찾을 수 없습니다."));
            product.decreaseStock(dto.getCnt()); // 변경 감지 -> 자동으로 업데이트
        }
        log.info("재고 감소 로직 완료");
    }

    @Transactional
    public void increaseStock(List<IncreaseStockReqDto> increaseStockReqDtos) {
        log.info("재고 증가 로직 시작");
        for (IncreaseStockReqDto dto : increaseStockReqDtos) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new BaseBizException("productID가 " + dto.getProductId() + "인 상품 옵션을 찾을 수 없습니다."));
            product.increaseStock(dto.getCnt());
        }
        log.info("재고 증가 로직 완료");
    }
}
