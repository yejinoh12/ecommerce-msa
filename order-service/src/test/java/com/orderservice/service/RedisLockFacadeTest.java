package com.orderservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.netflix.discovery.converters.Auto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLockFacadeTest {

    @Autowired
    private RedisStockService redisStockService;

    @Autowired
    private RedisLockFacade redisLockFacade;

    @BeforeEach
    public void BeforeEach() {
        //redisStockService.enrollStock(1L, 10);
    }

    @AfterEach
    public void AfterEach() {
        redisStockService.deleteStock(90L);
    }

    @Test
    @DisplayName("Redisson Test : 재고가 100개 일때, 고객 101명이 주문하는 경우")
    void RedisStockTest() throws InterruptedException {

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            executorService.submit(() -> {
                try {
                    redisLockFacade.updateStockRedisson(List.of(new UpdateStockReqDto(90L, 1, "DEC")));
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Integer redisStock = redisStockService.findProductStock(90L);

        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("Redis 재고: " + redisStock);

        assertAll(
                () -> assertThat(successCount.get()).isEqualTo(10),
                () -> assertThat(failCount.get()).isEqualTo(90),
                () -> assertThat(redisStock).isEqualTo(0) // Redis 재고 확인
        );
    }
}