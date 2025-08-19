package com.zenitron.znt_inbound_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zenitron.znt_inbound_api.dto.InboundOrderData;
import com.zenitron.znt_inbound_api.dto.InboundOrderDetail;
import com.zenitron.znt_inbound_api.dto.InboundOrderRequest;
import com.zenitron.znt_inbound_api.exception.SignatureVerificationException;
import com.zenitron.znt_inbound_api.repository.InboundOrderRepository;
import com.zenitron.znt_inbound_api.repository.PartnerKeyRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Service
public class InboundOrderService {

    private static final Logger logger = LoggerFactory.getLogger(InboundOrderService.class);

    private final PartnerKeyRepository partnerKeyRepository;
    private final InboundOrderRepository inboundOrderRepository;
    private final EmailService emailService;

    @Autowired
    public InboundOrderService(PartnerKeyRepository partnerKeyRepository, InboundOrderRepository inboundOrderRepository, EmailService emailService) {
        this.partnerKeyRepository = partnerKeyRepository;
        this.inboundOrderRepository = inboundOrderRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void processInboundOrder(InboundOrderRequest request) {
        verifySignature(request);

        String senderCode = partnerKeyRepository.findSenderCodeByPartCode(request.getPartCode())
                .orElseThrow(() -> {
                    logger.error("無法從 part_code '{}' 反向查找 sender code", request.getPartCode());
                    // 這通常是資料設定問題，所以拋出一個會導致 500 錯誤的例外
                    return new IllegalStateException("伺服器內部錯誤：無法解析發送方代碼");
                });

        Date jsonCreationDate = parseDate(request.getRequestTime(), "request_time", true);
        
        InboundOrderData data = request.getData();
        Date shippedTime = parseDate(data.getShippedTime(), "shipped_time", false);

        // 1. 為此請求獲取一個唯一的 SEQ_ID
        Long seqId = inboundOrderRepository.getNextSeqId();

        // 2. 將 Envelope 資料寫入，每個請求只寫一次
        inboundOrderRepository.saveEnvelope(
                seqId,
                senderCode,
                request.getPartCode(),
                request.getSign(),
                request.getRequestTime(),
                data.getRefNo(),
                data.getWmsNo(),
                data.getWmsUuid(),
                data.getBusiDate(),
                "SHIP", // messageType
                shippedTime // docDateTime
        );

        // 3. 遍歷所有明細，使用相同的 SEQ_ID 寫入 JSON_SHIP 表
        for (InboundOrderDetail detail : data.getDetail()) {
            inboundOrderRepository.saveJsonShip(seqId, senderCode, jsonCreationDate, data, detail);
        }

        logger.info("成功將 {} 筆資料存入資料庫，參考編號 (ref_no): {}", data.getDetail().size(), data.getRefNo());

        // 發送成功通知郵件
        List<String> recipients = partnerKeyRepository.findRecipientsBySenderCode(senderCode);
        emailService.sendSuccessEmail(recipients, senderCode, data.getRefNo(), data.getWmsNo(), "SHIP");

        logger.debug("完整請求內容: {}", request);
    }

    private Date parseDate(String dateString, String fieldName, boolean isRequired) {
        if (dateString == null || dateString.trim().isEmpty()) {
            if (isRequired) {
                throw new SignatureVerificationException(fieldName + " 不得為空");
            }
            return null;
        }
        try {
            // 假設日期格式為 'yyyy-MM-dd HH:mm:ss'
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dateString);
        } catch (ParseException e) {
            logger.error("無法解析日期欄位 '{}': '{}'", fieldName, dateString, e);
            throw new SignatureVerificationException("日期欄位 '"+ fieldName +"' 格式不正確，應為 'yyyy-MM-dd HH:mm:ss'");
        }
    }

    private void verifySignature(InboundOrderRequest request) {
        String partCode = request.getPartCode();
        String partnerKey = partnerKeyRepository.findPartnerKeyByPartCode(partCode)
                .orElseThrow(() -> {
                    logger.error("找不到 part_code '{}' 對應的 Partner Key", partCode);
                    return new SignatureVerificationException("無效的 part_code 或找不到對應的金鑰");
                });

        try {
            // 為了確保 JSON 序列化結果與客戶端請求的順序完全一致，
            // 我們建立一個新的 ObjectMapper 並明確停用字母排序功能。
            // 這樣可以強制 Jackson 遵循 DTO 中 @JsonPropertyOrder 定義的順序。
            ObjectMapper objectMapper = JsonMapper.builder()
                    .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .build();
            String dataJson = objectMapper.writeValueAsString(request.getData());
            
            // 印出 dataJson 以便檢查
            logger.info("=== 簽名驗證 Debug 資訊 ===");
            logger.info("dataJson: {}", dataJson);
            logger.info("dataJson 長度: {}", dataJson.length());
            logger.info("partnerKey: {}", partnerKey);
            
            String stringToSign = dataJson + partnerKey;
            logger.info("stringToSign 長度: {}", stringToSign.length());

            // 1. 計算 MD5 並取得其 32 字元的十六進位字串
            String md5Hex = DigestUtils.md5Hex(stringToSign);
            logger.info("MD5 Hex: {}", md5Hex);
            
            // 2. 將該十六進位字串進行 Base64 編碼
            String calculatedSignature = Base64.encodeBase64String(md5Hex.getBytes(StandardCharsets.UTF_8));
            logger.info("計算出的簽名: {}", calculatedSignature);
            logger.info("收到的簽名: {}", request.getSign());

            if (!calculatedSignature.equals(request.getSign())) {
                logger.warn("簽名驗證失敗。part_code: {}, 傳入簽名: {}, 計算後簽名: {}", partCode, request.getSign(), calculatedSignature);

                // 當簽名驗證失敗時，發送 Email 通知
                partnerKeyRepository.findSenderCodeByPartCode(partCode).ifPresent(senderCode -> {
                    List<String> recipients = partnerKeyRepository.findRecipientsBySenderCode(senderCode);
                    emailService.sendSignatureErrorEmail(
                        recipients,
                        senderCode,
                        partnerKey,
                        calculatedSignature,
                        request.getSign(),
                        "SHIP"
                    );
                });
                
                throw new SignatureVerificationException("簽名驗證失敗");
            }

            logger.info("part_code '{}' 的簽名驗證成功", partCode);

        } catch (JsonProcessingException e) {
            logger.error("將 'data' 物件序列化為 JSON 時發生錯誤", e);
            throw new SignatureVerificationException("請求資料格式錯誤");
        }
    }
} 