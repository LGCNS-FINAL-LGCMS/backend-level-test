package com.lgcms.leveltest.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${admin.email}")
    private String adminEmail;

    @Async
    public void sendCategoryNotificationToAdmin(String categoryName, String eventType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("[Level Test System] 카테고리 " + eventType + " 알림");
            message.setText(buildEmailContent(categoryName, eventType));

            javaMailSender.send(message);
            log.info("카테고리 {} 이벤트 알림 이메일을 Admin({})에게 전송 완료", eventType, adminEmail);

        } catch (Exception e) {
            log.error("Admin 이메일 전송 실패: 카테고리={}, 이벤트타입={}", categoryName, eventType, e);
        }
    }

    private String buildEmailContent(String categoryName, String eventType) {
        return String.format("""
            안녕하세요 Level Test 관리자님,
            
            Level Test 시스템에서 카테고리 관련 이벤트가 발생했습니다.
            
            ■ 이벤트 정보
            - 카테고리명: %s
            - 변경 유형: %s
            - 발생 시간: %s
            
            시스템 관리가 필요한 경우 확인 부탁드립니다.
            
            감사합니다.
            """,
                categoryName,
                eventType,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}
