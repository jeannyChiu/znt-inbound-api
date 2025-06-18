package com.zenitron.znt_inbound_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zenitron.znt_inbound_api.dto.InboundReceiveData;
import com.zenitron.znt_inbound_api.dto.InboundReceiveDetail;
import com.zenitron.znt_inbound_api.dto.InboundReceiveRequest;
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

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class InboundReceiveService {

    private static final Logger logger = LoggerFactory.getLogger(InboundReceiveService.class);

    private final PartnerKeyRepository partnerKeyRepository;
    private final InboundOrderRepository inboundOrderRepository;
    private final EmailService emailService;

    @Autowired
    public InboundReceiveService(PartnerKeyRepository partnerKeyRepository, InboundOrderRepository inboundOrderRepository, EmailService emailService) {
        this.partnerKeyRepository = partnerKeyRepository;
        this.inboundOrderRepository = inboundOrderRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void processInboundReceive(InboundReceiveRequest request) {
        verifySignature(request);

        String senderCode = partnerKeyRepository.findSenderCodeByPartCode(request.getPartCode())
                .orElseThrow(() -> {
                    logger.error("無法從 part_code '{}' 反向查找 sender code", request.getPartCode());
                    return new IllegalStateException("伺服器內部錯誤：無法解析發送方代碼");
                });

        Date jsonCreationDate = parseDate(request.getRequestTime(), "request_time", true);

        InboundReceiveData data = request.getData();
        Date arrivedTime = parseDate(data.getArrivedTime(), "arrived_time", false);

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
                "RECEIV", // messageType
                arrivedTime // docDateTime
        );

        // 3. 遍歷所有明細，使用相同的 SEQ_ID 寫入 JSON_RECEIVE 表
        for (InboundReceiveDetail detail : data.getDetail()) {
            inboundOrderRepository.saveJsonReceive(seqId, senderCode, jsonCreationDate, data, detail);
        }

        logger.info("成功將 {} 筆資料存入資料庫，參考編號 (ref_no): {}", data.getDetail().size(), data.getRefNo());

        // 發送成功通知郵件
        List<String> recipients = partnerKeyRepository.findRecipientsBySenderCode(senderCode);
        emailService.sendSuccessEmail(recipients, senderCode, data.getRefNo(), data.getWmsNo(), "RECEIVE");

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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dateString);
        } catch (ParseException e) {
            logger.error("無法解析日期欄位 '{}': '{}'", fieldName, dateString, e);
            throw new SignatureVerificationException("日期欄位 '"+ fieldName +"' 格式不正確，應為 'yyyy-MM-dd HH:mm:ss'");
        }
    }

    private void verifySignature(InboundReceiveRequest request) {
        String partCode = request.getPartCode();
        String partnerKey = partnerKeyRepository.findPartnerKeyByPartCode(partCode)
                .orElseThrow(() -> {
                    logger.error("找不到 part_code '{}' 對應的 Partner Key", partCode);
                    return new SignatureVerificationException("無效的 part_code 或找不到對應的金鑰");
                });

        try {
            ObjectMapper objectMapper = JsonMapper.builder()
                    .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .build();
            String dataJson = objectMapper.writeValueAsString(request.getData());
            
            logger.info("用來計算簽章的 dataJson: {}", dataJson);

            String stringToSign = dataJson + partnerKey;

            String md5Hex = DigestUtils.md5Hex(stringToSign);
            String calculatedSignature = Base64.encodeBase64String(md5Hex.getBytes(StandardCharsets.UTF_8));

            if (!calculatedSignature.equals(request.getSign())) {
                logger.warn("簽名驗證失敗。part_code: {}, 傳入簽名: {}, 計算後簽名: {}", partCode, request.getSign(), calculatedSignature);

                partnerKeyRepository.findSenderCodeByPartCode(partCode).ifPresent(senderCode -> {
                    List<String> recipients = partnerKeyRepository.findRecipientsBySenderCode(senderCode);
                    emailService.sendSignatureErrorEmail(
                        recipients,
                        senderCode,
                        partnerKey,
                        calculatedSignature,
                        request.getSign(),
                        "RECEIVE"
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