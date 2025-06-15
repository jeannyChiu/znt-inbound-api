package com.zenitron.znt_inbound_api.controller;

import com.zenitron.znt_inbound_api.dto.InboundOrderRequest;
import com.zenitron.znt_inbound_api.service.InboundOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbound-ship-out")
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    @Autowired
    public InboundOrderController(InboundOrderService inboundOrderService) {
        this.inboundOrderService = inboundOrderService;
    }

    @PostMapping
    public ResponseEntity<String> receiveInboundOrder(@Valid @RequestBody InboundOrderRequest request) {
        inboundOrderService.processInboundOrder(request);
        // 您可以根據業務需求自訂成功時的回應內容
        return ResponseEntity.ok("出庫單發運請求已成功接收");
    }
} 