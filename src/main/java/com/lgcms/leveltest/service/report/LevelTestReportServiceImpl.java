package com.lgcms.leveltest.service.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.*;
import com.lgcms.leveltest.dto.response.report.ReportDetailResponse;
import com.lgcms.leveltest.dto.response.report.ReportHistoryResponse;
import com.lgcms.leveltest.dto.response.scoring.ConceptAnalysis;
import com.lgcms.leveltest.repository.LevelTestReportRepository;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import com.lgcms.leveltest.repository.ReportConceptAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LevelTestReportServiceImpl implements LevelTestReportService {

    private final LevelTestReportRepository reportRepository;
    private final ReportConceptAnalysisRepository conceptAnalysisRepository;
    private final MemberAnswerRepository memberAnswerRepository;
    private final ObjectMapper objectMapper;
    private final ChatClient conceptAnalysisChatClient;
    private final ChatClient feedbackChatClient;

    @Override
    @Transactional
    public ReportDetailResponse createReport(Long memberId) {
        log.info("회원 {}의 새 레포트 생성 시작", memberId);

        // 채점 완료된 답변 조회
        List<MemberAnswer> allAnswers = memberAnswerRepository.findTop10ByMemberIdAndIsScoredOrderByIdDesc(memberId, true);

        if (allAnswers.size() < 10) {
            throw new BaseException(LevelTestError.GRADING_NOT_COMPLETED);
        }

        // 레포트 데이터 계산
        Integer totalScore = calculateTotalScore(allAnswers);
        StudentLevel studentLevel = determineStudentLevel(totalScore);
        String category = getCategoryFromAnswers(allAnswers);

        // 문제 기반으로 주요 개념 6개 추출
        List<ConceptAnalysis> fixedConcepts = extractFixedConceptsFromQuestions(allAnswers);

        // 추출된 개념들에 대해 수강생 답변 기반으로 점수 계산
        List<ConceptAnalysis> conceptSummaries = evaluateConceptsBasedOnAnswers(fixedConcepts, allAnswers);
        String comprehensiveFeedback = generateComprehensiveFeedback(memberId);
        String nextLearningRecommendation = generateLearningRecommendation(conceptSummaries, studentLevel);

        // 레포트 엔티티 생성 및 저장
        LevelTestReport report = LevelTestReport.builder()
                .memberId(memberId)
                .category(category)
                .totalScore(totalScore)
                .totalQuestions(allAnswers.size())
                .studentLevel(studentLevel)
                .comprehensiveFeedback(comprehensiveFeedback)
                .nextLearningRecommendation(nextLearningRecommendation)
                .build();

        LevelTestReport savedReport = reportRepository.save(report);

        // 개념 분석 저장
        List<ReportConceptAnalysis> conceptEntities = conceptSummaries.stream()
                .map(concept -> ReportConceptAnalysis.builder()
                        .report(savedReport)
                        .conceptName(concept.getConceptName())
                        .score(concept.getScore())
                        .comment(concept.getComment())
                        .build())
                .collect(Collectors.toList());

        conceptAnalysisRepository.saveAll(conceptEntities);

        log.info("회원 {}의 레포트 생성 완료. 레포트 ID: {}", memberId, savedReport.getId());

        // 응답 생성
        return ReportDetailResponse.builder()
                .reportId(savedReport.getId())
                .memberId(savedReport.getMemberId())
                .totalScore(savedReport.getTotalScore())
                .totalQuestions(savedReport.getTotalQuestions())
                .studentLevel(savedReport.getStudentLevel())
                .category(getCategoryFromAnswers(allAnswers))
                .conceptSummaries(conceptSummaries)
                .comprehensiveFeedback(savedReport.getComprehensiveFeedback())
                .nextLearningRecommendation(savedReport.getNextLearningRecommendation())
                .createdAt(savedReport.getCreatedAt())
                .build();
    }

    private List<ConceptAnalysis> extractFixedConceptsFromQuestions(List<MemberAnswer> answers) {
        // 10개 문제의 카테고리, 난이도, 핵심 키워드를 LLM에게 전달하여
        // 주요 개념 6개를 추출하도록 요청
        StringBuilder questionsInfo = new StringBuilder();
        for (MemberAnswer answer : answers) {
            LevelTest question = answer.getQuestion();
            questionsInfo.append(String.format(
                    "[문제 %d] %s - %s난이도\n문제: %s\n핵심키워드: %s\n\n",
                    answers.indexOf(answer) + 1,
                    question.getCategory().getCategoryName(),
                    question.getDifficulty().getDifficultyName(),
                    question.getQuestion(),
                    String.join(", ", question.getMustInclude())
            ));
        }

        String prompt = String.format("""
        다음 10개 문제를 분석하여 가장 중요한 6개 핵심 개념을 추출해주세요:
        
        %s
        
        주의: score는 임시값(1)으로 설정하세요. 실제 평가는 별도 단계에서 진행됩니다.
        
        JSON 형식으로 6개 개념만 반환:
        [
            {
                "conceptName": "구체적인 개념명 (예: Spring Bean 생명주기, 의존성 주입 등)",
                "score": 1,  // 임시값 (실제 평가는 다음 단계에서)
                "comment": "평가 대기중"
            }
        ]
        """, questionsInfo.toString());

        // AI 호출 및 파싱
        String response = conceptAnalysisChatClient.prompt().user(prompt).call().content();
        return parseConceptAnalysisFromResponse(response);
    }

    private List<ConceptAnalysis> evaluateConceptsBasedOnAnswers(List<ConceptAnalysis> fixedConcepts, List<MemberAnswer> answers) {
        // 각 고정 개념에 대해 수강생의 10개 답변을 종합 분석하여 점수 부여
        StringBuilder analysisData = new StringBuilder();

        // 수강생 답변 정보 구성
        for (MemberAnswer answer : answers) {
            analysisData.append(String.format(
                    "[문제 %d] %s (점수: %d점)\n답변: %s\n\n",
                    answers.indexOf(answer) + 1,
                    answer.getQuestion().getQuestion(),
                    answer.getScore() != null ? answer.getScore() : 0,
                    answer.getMemberAnswer() != null ? answer.getMemberAnswer() : "(답변 없음)"
            ));
        }

        String prompt = String.format("""
            <role>
            당신은 학생의 답변을 분석하여 기술 개념 이해도를 평가하는 AI 전문 분석가입니다.
            
            중요: 이 작업에서는 오직 "개념 이해도 별점"만 평가합니다.
            문제별 배점(studentScore)과는 완전히 다른 평가 기준입니다.
            </role>
            
            <score_system_clarification>
            점수 체계 구분
            
            사용하지 않는 점수: studentScore (8점/10점/12점 - 문제별 배점)
            사용하는 점수: 개념별 이해도 별점 (1~5점만)
            
            개념 이해도 별점 기준:
            1점: 전혀 이해하지 못함
            2점: 부족한 이해
            3점: 보통 이해
            4점: 좋은 이해
            5점: 완벽한 이해
            
            절대 규칙: 1,2,3,4,5 외의 다른 숫자는 금지 (0, 6, 7, 8, 소수점 등)
            </score_system_clarification>
            
            <answers>학생의 전체 답변 내용\n%s</answers>
            
            <concepts_to_evaluate>평가할 핵심 개념\n%s</concepts_to_evaluate>
            
            <understanding_rubric>
            개념 이해도 평가 기준 (별점 1-5)
            
            5별점 (완벽한 이해):
            - 관련된 모든 답변에서 개념을 매우 정확하고 상세하게 설명
            - 핵심 용어를 올바르게 사용하고 원리를 명확히 이해
            
            4별점 (좋은 이해):
            - 개념을 올바르게 이해하고 있으나 일부 답변에서 설명이 약간 부족
            - 전반적으로 올바른 방향이지만 사소한 오류나 누락 존재
            
            3별점 (보통 이해):
            - 개념의 기본적인 내용은 이해하고 있음
            - 중요한 부분을 놓치거나 일부 오해가 드러남
            
            2별점 (부족한 이해):
            - 관련된 키워드는 언급했으나 개념을 거의 설명하지 못함
            - 설명에 큰 오류가 있거나 이해도가 매우 제한적
            
            1별점 (전혀 이해하지 못함):
            - 관련된 답변이 없거나 "모르겠다"고 답변
            - 질문과 완전히 무관한 내용만 언급
            </understanding_rubric>
            
            <critical_instructions>
            절대적 지시사항
            1. 개념별 이해도는 반드시 1,2,3,4,5 중 하나의 정수만 사용
            2. 문제 배점(8-12점)과 절대 혼동하지 말 것
            3. 6점 이상, 0점 이하, 소수점은 시스템 오류 발생
            4. 각 개념별로 전체 답변 내용을 종합 분석하여 별점 부여
            5. JSON 출력 전 모든 별점이 1-5 범위인지 재확인 필수
            </critical_instructions>
            
            <output_format>
            필수 JSON 출력 형식
            [
                {
                    "conceptName": "Spring Bean 생명주기",
                    "score": 3,  // 반드시 1,2,3,4,5 중 하나
                    "comment": "기본 개념은 이해하나 세부사항 부족"
                },
                {
                    "conceptName": "의존성 주입(DI)",
                    "score": 4,  // 반드시 1,2,3,4,5 중 하나
                    "comment": "개념을 잘 이해하고 있음"
                }
            ]
            최종 점검: 출력 전 모든 score 값이 1-5 사이인지 확인!
            </output_format>
            """,
                analysisData.toString(),
                fixedConcepts.stream()
                        .map(c -> "- " + c.getConceptName())
                        .collect(Collectors.joining("\n"))
        );

        String response = conceptAnalysisChatClient.prompt().user(prompt).call().content();
        return parseConceptAnalysisFromResponse(response);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportHistoryResponse> getReportHistory(Long memberId) {
        log.info("회원 {}의 레포트 히스토리 조회", memberId);

        List<LevelTestReport> reports = reportRepository.findTop3ByMemberIdOrderByCreatedAtDesc(memberId);

        return reports.stream()
                .map(report -> ReportHistoryResponse.builder()
                        .reportId(report.getId())
                        .totalScore(report.getTotalScore())
                        .studentLevel(report.getStudentLevel())
                        .category(report.getCategory())
                        .createdAt(report.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDetailResponse getReportDetail(Long memberId, Long reportId) {
        log.info("레포트 ID {}의 상세 정보 조회", reportId);

        LevelTestReport report = reportRepository.findByIdWithConceptAnalyses(reportId)
                .orElseThrow(() -> new BaseException(LevelTestError.REPORT_NOT_FOUND));

        // 멤버ID 검증
        if (!report.getMemberId().equals(memberId)) {
            throw new BaseException(LevelTestError.UNAUTHORIZED_ACCESS);
        }

        // 개념 분석을 ConceptAnalysis DTO로 변환
        List<ConceptAnalysis> conceptSummaries = report.getConceptAnalyses().stream()
                .map(entity -> ConceptAnalysis.builder()
                        .conceptName(entity.getConceptName())
                        .score(entity.getScore())
                        .comment(entity.getComment())
                        .build())
                .collect(Collectors.toList());

        return ReportDetailResponse.builder()
                .reportId(report.getId())
                .memberId(report.getMemberId())
                .totalScore(report.getTotalScore())
                .totalQuestions(report.getTotalQuestions())
                .studentLevel(report.getStudentLevel())
                .category(report.getCategory())
                .conceptSummaries(conceptSummaries)
                .comprehensiveFeedback(report.getComprehensiveFeedback())
                .nextLearningRecommendation(report.getNextLearningRecommendation())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private String getCategoryFromAnswers(List<MemberAnswer> answers) {
        return answers.get(0).getQuestion().getCategory().getCategoryName();
    }

    private Integer calculateTotalScore(List<MemberAnswer> answers) {
        return answers.stream()
                .mapToInt(answer -> answer.getScore() != null ? answer.getScore() : 0)
                .sum();
    }

    private StudentLevel determineStudentLevel(Integer totalScore) {
        if (totalScore >= 90) return StudentLevel.HIGH;
        if (totalScore >= 40) return StudentLevel.MEDIUM;
        return StudentLevel.LOW;
    }

    private List<ConceptAnalysis> parseConceptAnalysisFromResponse(String response) {
        try {
            String cleaned = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "");

            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');

            if (start != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            List<ConceptAnalysis> concepts = objectMapper.readValue(cleaned,
                    new TypeReference<List<ConceptAnalysis>>() {});

            return concepts.stream()
                    .limit(6)
                    .map(concept -> ConceptAnalysis.builder()
                            .conceptName(concept.getConceptName())
                            .score(Math.max(1, Math.min(5, concept.getScore())))
                            .comment(concept.getComment())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("파싱 실패: {}", response, e);
            return new ArrayList<>();
        }
    }

    private String generateComprehensiveFeedback(Long memberId) {
        List<MemberAnswer> allAnswers = memberAnswerRepository.findTop10ByMemberIdAndIsScoredOrderByIdDesc(memberId, true);

        if (allAnswers.size() < 10) {
            return "아직 모든 문제의 채점이 완료되지 않았습니다. (" + allAnswers.size() + "/10)";
        }

        try {
            String promptText = buildComprehensiveFeedbackPrompt(allAnswers);

            return feedbackChatClient
                    .prompt()
                    .user(promptText)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Error generating comprehensive feedback for member: {}", memberId, e);
            return "종합 피드백 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private String buildComprehensiveFeedbackPrompt(List<MemberAnswer> answers) {
        double avgScore = answers.stream()
                .mapToInt(a -> a.getScore() != null ? a.getScore() : 0)
                .average()
                .orElse(0.0);

        String questionsAndAnswers = answers.stream()
                .map(answer -> String.format(
                        "[문제 %d] (%s - %s) %s\n[학생답변] %s\n[점수] %d점",
                        answers.indexOf(answer) + 1,
                        answer.getQuestion().getCategory().getCategoryName(),
                        answer.getQuestion().getDifficulty().getDifficultyName(),
                        answer.getQuestion().getQuestion(),
                        answer.getMemberAnswer(),
                        answer.getScore() != null ? answer.getScore() : 0
                ))
                .collect(Collectors.joining("\n\n"));

        return String.format("""
            다음은 학생의 프로그래밍 레벨테스트 전체 답변입니다.
            
            [전체 성과] 평균 점수: %.1f점 (총 %d문제)
            
            %s
            
            위 결과를 바탕으로 학생의 실력을 종합 평가하여 2-4줄의 간단한 피드백을 작성해주세요.
            다음 내용을 자연스럽게 포함하되 소제목 없이 하나의 문단으로 작성하세요:
            
            - 전반적인 실력 수준과 이해도
            - 실제 보여준 강점이 있다면 구체적으로 언급, 없다면 강점 언급하지 않음
            - 가장 시급한 개선점 2-3개 (구체적 근거와 함께)
            - 모든 평가는 실제 답변 내용과 점수를 근거로 작성
            - 구체적인 학습 방향 제시
            
            - 마크다운 형식이나 소제목 없이 일반 텍스트로만 작성하세요.
            - 2-4문장으로 간결하게 작성하세요.
            - '이 학생은' 같은 3인칭 표현은 사용하지 말고, 주어를 생략하거나 직접적인 어조로 작성하세요.
            """, avgScore, answers.size(), questionsAndAnswers);
    }

    private String generateLearningRecommendation(List<ConceptAnalysis> conceptSummaries, StudentLevel studentLevel) {
        StringBuilder recommendation = new StringBuilder();

        // 점수가 낮은 개념들 식별 (1-2점)
        List<ConceptAnalysis> weakConcepts = conceptSummaries.stream()
                .filter(concept -> concept.getScore() <= 2)
                .toList();

        if (!weakConcepts.isEmpty()) {
            recommendation.append("우선 학습이 필요한 영역은 ");
            recommendation.append(weakConcepts.stream()
                    .map(ConceptAnalysis::getConceptName)
                    .collect(Collectors.joining(", ")));
            recommendation.append("입니다. ");
        }

        // 레벨별 맞춤 추천
        switch (studentLevel) {
            case HIGH -> {
                recommendation.append("고급 과정에서는 실무 프로젝트 참여와 포트폴리오 구축을 통해 역량을 확장하고, 아키텍처 설계와 성능 최적화 학습에 집중하며, 오픈소스 기여나 기술 블로그 작성을 통해 경험을 넓혀가는 것이 좋습니다.");
            }
            case MEDIUM -> {
                recommendation.append("중급 수준에서는 핵심 개념을 보강하고 심화 학습을 진행하며, 실습 위주의 프로젝트 경험을 쌓고 코드 리뷰와 협업 경험을 늘려가는 것이 도움이 됩니다.");
            }
            case LOW -> {
                recommendation.append("기초 단계에서는 기본 개념을 차근차근 학습하고 간단한 예제와 실습을 반복하며, 멘토링이나 스터디 그룹에 참여하여 학습 동기를 유지하는 것이 좋습니다.");
            }
        }

        return recommendation.toString();
    }
}