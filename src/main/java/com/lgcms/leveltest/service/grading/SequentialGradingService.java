package com.lgcms.leveltest.service.grading;

import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.service.report.LevelTestReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SequentialGradingService {

    private final GradingService gradingService;
    private final LevelTestReportService reportService;

    @Async("gradingExecutor")
    public void gradeAllAnswersSequentially(Long memberId, List<MemberAnswer> answers) {
        log.info("회원 {}의 답변 순차 채점 시작. 총 {}문제", memberId, answers.size());

        int successCount = 0;
        for (int i = 0; i < answers.size(); i++) {
            MemberAnswer answer = answers.get(i);
            try {
                gradingService.gradeAnswer(answer);
                successCount++;
                log.info("채점 진행: {}/{} 완료", i + 1, answers.size());

                // Rate limit 방지를 위한 대기
                if (i < answers.size() - 1) {
                    Thread.sleep(5000); // 3초 간격
                }
            } catch (Exception e) {
                log.error("답변 ID {} 채점 실패: {}", answer.getId(), e.getMessage());
            }
        }

        // 모든 채점 완료 후 자동으로 레포트 생성
        if (successCount == answers.size() && successCount >= 10) {
            try {
                reportService.createReport(memberId);
                log.info("회원 {}의 레포트 자동 생성 완료", memberId);
            } catch (Exception e) {
                log.error("레포트 자동 생성 실패: {}", e.getMessage());
            }
        }

        log.info("회원 {}의 순차 채점 완료. 성공: {}/{}", memberId, successCount, answers.size());
    }
}