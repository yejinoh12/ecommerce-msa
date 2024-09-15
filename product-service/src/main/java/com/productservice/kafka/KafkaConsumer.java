package com.productservice.kafka;


import com.common.dto.order.UpdateStockReqDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.productservice.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final StockService stockService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock-decrease-topic", groupId = "product-group")
    public void listenStockDecreaseRequest(String payload) {

        try {
            UpdateStockReqDto updateStockReqDtos = objectMapper.readValue(payload, UpdateStockReqDto.class);
            stockService.decreaseDBStock(updateStockReqDtos);
        } catch (JsonProcessingException e) {
            log.error("재고 업데이트 요청 처리 중 오류 발생", e);
            throw new RuntimeException("재고 업데이트 메시지 처리 실패", e);
        } catch (Exception e) {
            log.error("재고 감소 중 오류 발생", e);
            throw new RuntimeException("재고 업데이트 실패", e);
        }
    }

    @KafkaListener(topics = "stock-increase-topic", groupId = "product-group")
    public void listenStockIncreaseRequest(String payload) {

        try {
            UpdateStockReqDto updateStockReqDtos = objectMapper.readValue(payload, UpdateStockReqDto.class);
            stockService.increaseDBStock(updateStockReqDtos);
        } catch (JsonProcessingException e) {
            log.error("재고 업데이트 요청 처리 중 오류 발생", e);
            throw new RuntimeException("재고 업데이트 메시지 처리 실패", e);
        } catch (Exception e) {
            log.error("재고 증가 중 오류 발생", e);
            throw new RuntimeException("재고 업데이트 실패", e);
        }
    }
}