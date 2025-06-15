package com.zenitron.znt_inbound_api.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PartnerKeyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PartnerKeyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根據 part_code (API傳入的發送方ID, 對應到B2B.ZEN_B2B_TAB_D.TD_NAME) 查找 Partner Key (TD_NAME)。
     * 查詢邏輯：
     * 1. 使用 part_code 在 TD_NAME 欄位中找到對應的 ID 記錄 (TD_SEQ=1)。
     * 2. 從該記錄的 TD_NO (例如 "FEILIKS-ID") 推導出金鑰記錄的 TD_NO (例如 "FEILIKS-KEY")。
     * 3. 查找金鑰記錄 (TD_SEQ=2) 並返回其 TD_NAME 作為金鑰。
     * @param partCode API 請求中的 part_code, 例如 "ZENHUBFEILIKS"
     * @return 合作夥伴金鑰
     */
    public Optional<String> findPartnerKeyByPartCode(String partCode) {
        String sql = "SELECT t2.TD_NAME " +
                     "FROM B2B.ZEN_B2B_TAB_D t2 " +
                     "WHERE t2.T_NO = 'JSON_SYS_INFO' " +
                     "  AND t2.TD_SEQ = 2 " +
                     "  AND t2.TD_NO = (" +
                     "    SELECT REPLACE(t1.TD_NO, '-ID', '-KEY') " +
                     "    FROM B2B.ZEN_B2B_TAB_D t1 " +
                     "    WHERE t1.T_NO = 'JSON_SYS_INFO' " +
                     "      AND t1.TD_SEQ = 1 " +
                     "      AND t1.TD_NAME = ?" +
                     "  )";
        try {
            String key = jdbcTemplate.queryForObject(sql, String.class, partCode);
            return Optional.ofNullable(key);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 根據 part_code (TD_NAME) 反向查找發送方代碼 (例如 "FEILIKS")。
     * @param partCode API 請求中的 part_code
     * @return 發送方代碼
     */
    public Optional<String> findSenderCodeByPartCode(String partCode) {
        String sql = "SELECT TD_NO FROM B2B.ZEN_B2B_TAB_D " +
                     "WHERE T_NO = 'JSON_SYS_INFO' AND TD_SEQ = 1 AND TD_NAME = ?";
        try {
            String tdNo = jdbcTemplate.queryForObject(sql, String.class, partCode);
            // 從 "FEILIKS-ID" 中解析出 "FEILIKS"
            if (tdNo != null && tdNo.endsWith("-ID")) {
                return Optional.of(tdNo.substring(0, tdNo.length() - 3));
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 根據發送方代碼查找需要接收通知的郵件地址列表。
     * @param senderCode 發送方代碼，例如 "FEILIKS"
     * @return 收件人 Email 列表
     */
    public List<String> findRecipientsBySenderCode(String senderCode) {
        String sql = "SELECT TD_NO FROM B2B.ZEN_B2B_TAB_D " +
                     "WHERE T_NO = 'JSON_RECEIV_MAIL' AND TD_NAME1 = ?";
        return jdbcTemplate.queryForList(sql, String.class, senderCode);
    }
}