package com.productservice.service.stock;


import com.productservice.domain.product.Product;
import com.productservice.repository.product.ProductRepository;
import com.productservice.repository.product.RedisStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisStockRepository redisStockRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void enrollStock(Long productId, int quantity) {
        redisStockRepository.save(productId, quantity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purchase(Long productId, int quantity) {
        Integer currentStock = loadDataIfAbsentInCache(productId);
        if (currentStock < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }else{
            redisStockRepository.decrementStock(productId, quantity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancel(Long productId, int quantity) {
        loadDataIfAbsentInCache(productId);
        redisStockRepository.incrementStock(productId, quantity);
    }

    @Transactional
    public Optional<Integer> findProductStock(Long productId) {
        return redisStockRepository.findProductStock(productId);
    }

    /**
     * 캐시에서 재고 정보를 가져오거나 없을 시 DB에서 조회 후 캐시에 저장
     * @param productId 상품 ID
     * @return 현재 재고
     */

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
