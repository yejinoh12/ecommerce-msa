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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final StockService stockService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock-decrease-topic", groupId = "product-group")
    public void listenStockDecreaseRequest(String payload) throws JsonProcessingException {

        // 메시지 역직렬화
        List<UpdateStockReqDto> updateStockReqDtos =
                objectMapper.readValue(payload, new TypeReference<List<UpdateStockReqDto>>() {});

        // 각 항목에 대해 재고 감소 처리
        for (UpdateStockReqDto updateStockReqDto : updateStockReqDtos) {
            stockService.decreaseDBStock(updateStockReqDto);
        }
    }

    @KafkaListener(topics = "stock-increase-topic", groupId = "product-group")
    public void listenStockIncreaseRequest(String payload) throws JsonProcessingException {

        // 메시지 역직렬화
        List<UpdateStockReqDto> updateStockReqDtos =
                objectMapper.readValue(payload, new TypeReference<List<UpdateStockReqDto>>() {});

        // 각 항목에 대해 재고 증가 처리
        for (UpdateStockReqDto updateStockReqDto : updateStockReqDtos) {
            stockService.increaseDBStock(updateStockReqDto);
        }
    }
}