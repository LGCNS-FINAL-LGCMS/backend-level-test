package com.lgcms.leveltest.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class QuestionRequestLogService {

    // 메모리 캐시: {회원ID: {문제ID 세트, 요청시간}}
    private final Map<Long, RequestRecord> userQuestionCache = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    private static class RequestRecord {
        private Set<Long> questionIds;
        private LocalDateTime requestTime;
    }

    public void logQuestionRequest(Long memberId, List<Long> questionIds) {
        RequestRecord record = new RequestRecord(
                new HashSet<>(questionIds),
                LocalDateTime.now()
        );

        userQuestionCache.put(memberId, record);

        log.info("회원 {}의 문제 요청 기록됨: {}", memberId, questionIds);

        // 30분 후 자동 삭제 스케줄링
        scheduleCleanup(memberId);
    }

    public boolean validateQuestionAccess(Long memberId, List<Long> submittedQuestionIds) {
        RequestRecord record = userQuestionCache.get(memberId);

        // 요청 기록이 없음
        if (record == null) {
            log.warn("회원 {}의 문제 요청 기록이 없음", memberId);
            return false;
        }

        // 30분 경과 확인
        if (record.getRequestTime().isBefore(LocalDateTime.now().minusMinutes(30))) {
            log.warn("회원 {}의 문제 요청 기록이 만료됨", memberId);
            userQuestionCache.remove(memberId); // 만료된 기록 삭제
            return false;
        }

        // 문제 ID 매칭 확인
        Set<Long> requestedQuestions = record.getQuestionIds();
        Set<Long> submittedQuestions = new HashSet<>(submittedQuestionIds);

        boolean isMatch = requestedQuestions.equals(submittedQuestions);

        if (isMatch) {
            log.info("회원 {}의 문제 매칭 성공: 요청={}, 제출={}", memberId, requestedQuestions, submittedQuestions);
            // 검증 성공 시 기록 삭제 (재사용 방지)
            userQuestionCache.remove(memberId);
        } else {
            log.warn("회원 {}의 문제 매칭 실패: 요청={}, 제출={}", memberId, requestedQuestions, submittedQuestions);
        }

        return isMatch;
    }

    private void scheduleCleanup(Long memberId) {
        CompletableFuture.delayedExecutor(30, TimeUnit.MINUTES)
                .execute(() -> {
                    userQuestionCache.remove(memberId);
                    log.info("회원 {}의 문제 요청 기록 만료로 삭제", memberId);
                });
    }
}