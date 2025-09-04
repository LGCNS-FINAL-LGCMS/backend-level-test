package com.lgcms.leveltest.event.producer;


import com.lgcms.leveltest.common.kafka.dto.KafkaEvent;
import com.lgcms.leveltest.common.kafka.dto.LevelTestReportRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LevelTestEventProducer {

    @Value("${spring.application.name}")
    private String applicationName;

    private final KafkaTemplate kafkaTemplate;

    public void LevelTestReportRequested(LevelTestReportRequested levelTestReportRequested){

        KafkaEvent kafkaEvent = KafkaEvent.builder()
                .eventId(applicationName + UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now().toString())
                .eventType("LEVELTEST_REPORT_REQUESTED")
                .data(levelTestReportRequested)
                .build();

        kafkaTemplate.send("NOTIFICATION",kafkaEvent);
    }
}
