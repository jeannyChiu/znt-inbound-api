package com.zenitron.znt_inbound_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String dbUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.from}") String fromEmail, @Value("${spring.datasource.url}") String dbUrl) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.dbUrl = dbUrl;
    }

    private String getEnvironmentPrefix() {
        if (dbUrl != null) {
            if (dbUrl.contains("10.1.1.15")) {
                return "{Testing, Please Ignore} ";
            } else if (dbUrl.contains("10.1.1.144")) {
                return "{Production, Please Check} ";
            }
        }
        return "";
    }

    public void sendSignatureErrorEmail(List<String> to, String senderCode, String partnerKey, String calculatedSign, String receivedSign) {
        if (to == null || to.isEmpty()) {
            logger.warn("簽名驗證失敗，但找不到收件人，不發送郵件。Sender Code: {}", senderCode);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to.toArray(new String[0]));

            String subject = String.format("%sReceive [%s] EDI, SHIP key : [%s], sign Issue", getEnvironmentPrefix(), senderCode, partnerKey);
            message.setSubject(subject);

            String body = String.format(
                "<<Check Sign>>\n%s\n\n<<JOSN Sign>>\n%s\n\n***EDI系統自動通知請勿回覆!***",
                calculatedSign,
                receivedSign
            );
            message.setText(body);

            mailSender.send(message);
            logger.info("已發送簽名錯誤通知郵件至: {}", String.join(", ", to));

        } catch (Exception e) {
            logger.error("發送簽名錯誤通知郵件時發生錯誤。Sender Code: {}", senderCode, e);
        }
    }

    public void sendSuccessEmail(List<String> to, String senderCode, String refNo, String wmsNo) {
        if (to == null || to.isEmpty()) {
            logger.warn("資料處理成功，但找不到收件人，不發送成功通知郵件。Sender Code: {}", senderCode);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to.toArray(new String[0]));

            String subject = String.format("%sReceive SHIP REF#[%s],WMS_NO#[%s] from [%s]", getEnvironmentPrefix(), refNo, wmsNo, senderCode);
            message.setSubject(subject);

            String body = "***EDI系統自動通知請勿回覆!***";
            message.setText(body);

            mailSender.send(message);
            logger.info("已發送成功通知郵件至: {}", String.join(", ", to));

        } catch (Exception e) {
            logger.error("發送成功通知郵件時發生錯誤。Sender Code: {}", senderCode, e);
        }
    }
}