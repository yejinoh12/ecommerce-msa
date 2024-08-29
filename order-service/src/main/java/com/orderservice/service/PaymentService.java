package com.orderservice.service;

import com.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class PaymentService {

    private final Random random = new Random();

    /**
     * 결제 프로세스 중 고객 이탈율 시물레이션
     * [결제 시도] 결제 화면까지 왔지만 결제를 하지 않음 (고객 변심 이탈률 20%)
     * [결제 실패] 결제 이후 PG사 처리를 기다리는 중이었지만 고객 사유 또는 카드사 장애 등의 이유로 결제 실패(이탈률 20%)
     */

    public ApiResponse<?> simulatePaymentProcessing(Long orderId) {

        /**
         * 결제 까지 왔지만 결제를 취소
         */
        boolean paymentCancelled = random.nextInt(100) < 20;
        if (paymentCancelled) {
            log.info("결제 취소: 주문 ID = {}", orderId);
            return ApiResponse.error(400, "결제 취소됨");
        }

        /**
         * 결제가 실패한 경우
         */

        boolean paymentSuccessful = random.nextInt(100) >= 20; // 80% 확률로 결제 성공
        if (paymentSuccessful) {
            return ApiResponse.ok(200, "결제 성공", null);
        } else {
            log.error("결제 실패: 주문 ID = {}", orderId);
            return ApiResponse.error(500, "결제 실패. 나중에 다시 시도해 주세요.");
        }
    }
}