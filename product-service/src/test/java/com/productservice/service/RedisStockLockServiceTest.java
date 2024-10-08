//package com.productservice.service;
//
//import com.common.dto.order.UpdateStockReqDto;
//import com.productservice.domain.Product;
//import com.productservice.repository.ProductRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class RedisStockLockServiceTest {
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private RedisStockLockService redisStockLockService;
//
//    @Autowired
//    private RedisStockService redisStockService;
//
//    private Product product;
//
//    @BeforeEach
//    public void BeforeEach() {
//        product = new Product("product", 10,10);
//        productRepository.save(product);
//    }
//
//    @AfterEach
//    public void AfterEach(){
//        productRepository.delete(product);
//        redisStockService.deleteStock(product.getId());
//    }
//
//    @Test
//    public void RedissonLockTestV1() {
//        redisStockLockService.updateStockRedisson(List.of(new UpdateStockReqDto(product.getId(), 100, "DEC")));
//        Product p1 = productRepository.findById(product.getId()).orElseThrow();
//        assertEquals(0, p1.getStock());
//    }
//
//    @Test
//    @DisplayName("Redisson Test : 재고가 100개 일때, 고객 101명이 주문하는 경우")
//    void RedissonLockTest2() throws InterruptedException {
//
//        int threadCount = 100;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failCount = new AtomicInteger();
//
//        for (int i = 0; i < threadCount; i++) {
//
//            executorService.submit(() -> {
//                try {
//                    redisStockLockService.updateStockRedisson(List.of(new UpdateStockReqDto(product.getId(), 1, "INC")));
//                    successCount.getAndIncrement();
//                } catch (Exception e) {
//                    failCount.getAndIncrement();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//        executorService.shutdown();
//
//        Product p1 = productRepository.findById(product.getId()).orElseThrow();
//        Integer redisStock = redisStockService.findProductStock(product.getId()).orElse(null);
//
//        System.out.println("성공 횟수: " + successCount.get());
//        System.out.println("실패 횟수: " + failCount.get());
//        System.out.println("DB 재고: " + p1.getStock());
//        System.out.println("Redis 재고: " + redisStock);
//
//        assertAll(
//                () -> assertThat(successCount.get()).isEqualTo(100),
//                () -> assertThat(failCount.get()).isEqualTo(0),
//                () -> assertThat(p1.getStock()).isEqualTo(110) // DB 재고 확인
//               // () -> assertThat(redisStock).isEqualTo(100) // Redis 재고 확인
//        );
//    }
//}