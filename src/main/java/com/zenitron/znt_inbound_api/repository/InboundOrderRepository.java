package com.zenitron.znt_inbound_api.repository;

import com.zenitron.znt_inbound_api.dto.InboundOrderData;
import com.zenitron.znt_inbound_api.dto.InboundOrderDetail;
import com.zenitron.znt_inbound_api.dto.InboundOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class InboundOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public InboundOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long getNextSeqId() {
        String seqIdSql = "SELECT B2B.ZEN_B2B_SEQ.NEXTVAL FROM DUAL";
        Long nextSeqId = jdbcTemplate.queryForObject(seqIdSql, Long.class);

        if (nextSeqId == null) {
            throw new IllegalStateException("無法從序列 B2B.ZEN_B2B_SEQ 獲取新的 SEQ_ID");
        }
        return nextSeqId;
    }

    public void saveJsonShip(Long seqId, String senderCode, Date creationDate, InboundOrderData data, InboundOrderDetail detail) {
        String sql = "INSERT INTO B2B.ZEN_B2B_JSON_SHIP (" +
                "SEQ_ID, SENDER_CODE, RECEIVER_CODE, CREATION_DATE, REF_NO, SHIPPED_TIME, WMS_NO, WMS_UUID, BUSI_DATE, " +
                "BUYER_SKU_CODE, C_LPN_SEQ, C_LPN_UUID, C_VOL_HEIGHT, C_VOL_LENGTH, C_VOL_WIDTH, DATE_CODE, GROSS_WEIGHT, " +
                "LOT_NO, NET_WEIGHT, ORIGIN_COUNTRY_ENAME, OUT_PO_LINE, OUT_PO_NO, QTY, REF_NO1, SHIPPING_MARK, " +
                "SKU_CODE, STOCK_MODE, STOCK_UUID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                seqId,
                senderCode,
                "ZEN", // RECEIVER_CODE
                creationDate,
                data.getRefNo(),
                data.getShippedTime(),
                data.getWmsNo(),
                data.getWmsUuid(),
                data.getBusiDate(),
                detail.getBuyerSkuCode(),
                detail.getCLpnSeq(),
                detail.getCLpnUuid(),
                detail.getCVolHeight(),
                detail.getCVolLength(),
                detail.getCVolWidth(),
                detail.getDateCode(),
                detail.getGrossWeight(),
                detail.getLotNo(),
                detail.getNetWeight(),
                detail.getOriginCountryEname(),
                detail.getOutPoLine(),
                detail.getOutPoNo(),
                detail.getQty(),
                detail.getRefNo(), // detail's ref_no maps to REF_NO1
                detail.getShippingMark(),
                detail.getSkuCode(),
                detail.getStockMode(),
                detail.getStockUuid()
        );
    }

    public void saveEnvelope(Long seqId, String senderCode, InboundOrderRequest request, Date docDateTime) {
        InboundOrderData data = request.getData();
        String sql = "INSERT INTO B2B.ZEN_B2B_ENVELOPE (" +
                "SEQ_ID, SENDER_CODE, RECEIVER_CODE, SENDER_AP_ID, RECEIVER_AP_ID, SENDER_GS_ID, RECEIVER_GS_ID, " +
                "INTERCHANGE_NO, GROUP_NO, TRANSACTION_NO, DOC_NO, DOC_ID, DIRECTION, B2B_MSG_TYPE, DATASOURCE, " +
                "DOC_DATETIME, BATCH_PROCESS_ID, TRANS_FLAG, CONVERSATION_ID, CREATION_DATE, LAST_UPDATE_DATE, " +
                "REF_NO1, REF_NO2) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Date now = new Date();

        jdbcTemplate.update(sql,
                seqId,                          // SEQ_ID
                senderCode,                     // SENDER_CODE
                "ZEN",                          // RECEIVER_CODE
                request.getPartCode(),          // SENDER_AP_ID
                "ZEN",                          // RECEIVER_AP_ID
                senderCode,                     // SENDER_GS_ID
                "ZEN",                          // RECEIVER_GS_ID
                null,                           // INTERCHANGE_NO
                null,                           // GROUP_NO
                data.getRefNo(),                // TRANSACTION_NO
                data.getWmsNo(),                // DOC_NO
                String.valueOf(seqId),          // DOC_ID
                "IN",                           // DIRECTION
                "SHIP",                         // B2B_MSG_TYPE
                "EDI",                          // DATASOURCE
                docDateTime,                    // DOC_DATETIME
                data.getWmsUuid(),              // BATCH_PROCESS_ID
                "S",                            // TRANS_FLAG
                request.getSign(),              // CONVERSATION_ID
                now,                            // CREATION_DATE
                now,                            // LAST_UPDATE_DATE
                request.getRequestTime(),       // REF_NO1
                data.getBusiDate()              // REF_NO2
        );
    }
} 