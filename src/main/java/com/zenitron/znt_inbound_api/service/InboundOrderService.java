package com.zenitron.znt_inbound_api.service;

import com.zenitron.znt_inbound_api.dto.InboundOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InboundOrderService {

    private static final Logger logger = LoggerFactory.getLogger(InboundOrderService.class);

    public void processInboundOrder(InboundOrderRequest request) {
        // TODO: 在這裡實現業務邏輯，例如：
        // 1. 驗證簽名 (request.getSign())
        // 2. 檢查 part_code 是否合法
        // 3. 將資料存入資料庫
        // 4. 呼叫其他內部系統 API

        logger.info("成功接收到入庫單請求，參考編號 (ref_no): {}", request.getData().getRefNo());
        logger.debug("完整請求內容: {}", request);

        // 為了演示，我們暫時只記錄日誌。
    }
} 