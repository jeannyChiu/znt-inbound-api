package com.zenitron.znt_inbound_api.controller;

import com.zenitron.znt_inbound_api.dto.InboundReceiveRequest;
import com.zenitron.znt_inbound_api.service.InboundReceiveService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbound-receive")
public class InboundReceiveController {

    private final InboundReceiveService inboundReceiveService;

    @Autowired
    public InboundReceiveController(InboundReceiveService inboundReceiveService) {
        this.inboundReceiveService = inboundReceiveService;
    }

    @PostMapping
    public ResponseEntity<String> receiveInboundData(@Valid @RequestBody InboundReceiveRequest request) {
        inboundReceiveService.processInboundReceive(request);
        return ResponseEntity.ok("收貨數據回傳請求已成功接收");
    }
} 