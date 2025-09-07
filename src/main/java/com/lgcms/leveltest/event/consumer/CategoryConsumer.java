package com.lgcms.leveltest.event.consumer;

import com.lgcms.leveltest.common.kafka.dto.CategoryEvent;
import com.lgcms.leveltest.common.kafka.dto.KafkaEvent;
import com.lgcms.leveltest.common.kafka.util.KafkaEventFactory;
import com.lgcms.leveltest.service.email.EmailService;
import com.lgcms.leveltest.service.report.LevelTestReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryConsumer {

    private final KafkaEventFactory kafkaEventFactory;
    private final EmailService emailService;

    @KafkaListener(topics = "CATEGORY")
    public void handleCategoryEvent(KafkaEvent<?> event) {
        try {
            log.info("카테고리 이벤트 수신: eventId={}, eventType={}",
                    event.getEventId(), event.getEventType());

            CategoryEvent categoryEvent = kafkaEventFactory.convert(event, CategoryEvent.class);
            String eventType = event.getEventType();
            String categoryName = categoryEvent.getCategoryName();

            // eventType에 따른 분기 처리
            if ("CATEGORY_CREATED".equals(eventType)) {
                handleCategoryCreated(categoryName, eventType);

            } else if ("CATEGORY_MODIFIED".equals(eventType)) {
                handleCategoryModified(categoryName, eventType);

            } else {
                log.warn("처리되지 않는 이벤트 타입: {}, 카테고리: {}", eventType, categoryName);
            }

        } catch (Exception e) {
            log.error("카테고리 이벤트 처리 중 오류 발생: eventType={}",
                    event.getEventType(), e);
        }
    }

    private void handleCategoryCreated(String categoryName, String eventType) {
        log.info("새 카테고리 생성됨: {}", categoryName);
        emailService.sendCategoryNotificationToAdmin(categoryName, eventType);
    }

    private void handleCategoryModified(String categoryName, String eventType) {
        log.info("카테고리 수정됨: {}", categoryName);
        emailService.sendCategoryNotificationToAdmin(categoryName, eventType);
    }
}
