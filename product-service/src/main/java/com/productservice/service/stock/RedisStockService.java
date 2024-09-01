package com.productservice.service.stock;


import com.common.exception.BaseBizException;
import com.productservice.domain.product.Product;
import com.productservice.repository.product.ProductRepository;
import com.productservice.repository.product.RedisStockRepository;
import com.productservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;


import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisStockRepository redisStockRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void enrollStock(Long productId, int quantity) {
        redisStockRepository.save(productId, quantity);
    }

    @Transactional
    public void purchase(Long productId, int quantity) {

        Integer currentStock = loadDataIfAbsentInCache(productId);
        log.info("redis 재고 감소 시작, productId = {}, stock = {} ", productId, currentStock);

        if (currentStock < quantity) {
            throw new BaseBizException("재고가 부족합니다.");
        }

        redisStockRepository.decrementStock(productId, quantity);

        log.info("redis 재고 감소 완료, productId = {}, stock = {}", productId, currentStock - quantity);
    }

    /**
     *  redis 트랜젝션 내의 get 은 null을 반환할 수 있다.
     *  spring redis transaction 을 걸게 되면, multi-exec이 걸리게 되고 그 사이에 get을 한다면, exec구문이 끝난 뒤에 return이 되기 때문
     **/
    @Transactional
    public void cancel(Long productId, int quantity) {

        Integer currentStock = loadDataIfAbsentInCache(productId);

        log.info("redis 재고 증가 시작, productId = {}, stock = {} ", productId, currentStock);
        redisStockRepository.incrementStock(productId, quantity);
        log.info("redis 재고 증가 완료, productId = {}, stock = {} ", productId, currentStock + quantity);
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
//
//        product.setStock(currentStock + quantity);
//        productRepository.saveAndFlush(product);
//        log.info("DB 재고 동기화: productId={}, Redis 재고={}, DB 재고 = {}", productId, currentStock, product.getStock());
    }

    @Transactional
    public Optional<Integer> findProductStock(Long productId) {
        return redisStockRepository.findProductStock(productId);
    }

    @Transactional
    public void deleteStock(Long productId) {
        redisStockRepository.delete(productId); //junit test 후 삭제 시키기 위함
    }

    public Integer loadDataIfAbsentInCache(Long productId) {

        Integer currentStock = redisStockRepository.findProductStock(productId)
                .orElse(null);

        if (currentStock == null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("제품을 찾을 수 없습니다."));
            currentStock = product.getStock();
            redisStockRepository.save(productId, currentStock);
        }

        return currentStock;
    }
}
