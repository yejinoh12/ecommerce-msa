package com.productservice.service.stock;

import com.common.dto.order.DecreaseStockReqDto;
import com.common.dto.order.IncreaseStockReqDto;
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

    public ApiResponse<ProductStock> getProductStock(Long productId) {
        Product product = findProductById(productId);
        ProductStock productStock = new ProductStock(productId, product.getStock());
        return ApiResponse.ok(200, "제품 재고 조회 성공", productStock);
    }

    Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
    }

    /**********************************************************
     * 데이터 동기화 작업
     **********************************************************/

    @Transactional
    public void synchronizeStock(Long productId) {
        try {

            log.info("재고 동기화 시작");
            Integer currentRedisStock = redisStockService.findProductStock(productId).orElse(null);

            if (currentRedisStock == null) {

                Product product = productService.findProductById(productId);
                int dbStock = product.getStock();
                redisStockService.enrollStock(productId, dbStock);
                log.info("Redis에 재고 등록: productId={}, DB 재고={}", productId, dbStock);

            } else {
                // Redis 재고와 DB 재고를 맞춘다
                Product product = productService.findProductById(productId);
                int dbStock = product.getStock();

                if (!currentRedisStock.equals(dbStock)) {
                    product.setStock(currentRedisStock);
                    productRepository.saveAndFlush(product);
                    log.info("DB 재고 동기화: productId={}, Redis 재고={}, DB 재고={}", productId, currentRedisStock, dbStock);
                }
            }

        } catch (Exception e) {
            log.error("재고 동기화 실패: productId={}", productId, e);
        }
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
}
