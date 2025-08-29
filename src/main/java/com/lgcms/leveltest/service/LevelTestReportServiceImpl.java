package com.lgcms.leveltest.service;

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
        List<MemberAnswer> allAnswers = memberAnswerRepository.findByMemberIdAndIsScored(memberId, true);

        if (allAnswers.size() < 10) {
            throw new BaseException(LevelTestError.GRADING_NOT_COMPLETED);
        }

        // 레포트 데이터 계산
        Integer totalScore = calculateTotalScore(allAnswers);
        StudentLevel studentLevel = determineStudentLevel(totalScore);
        List<ConceptAnalysis> conceptSummaries = getConceptSummariesFromAI(memberId);
        String comprehensiveFeedback = generateComprehensiveFeedback(memberId);
        String nextLearningRecommendation = generateLearningRecommendation(conceptSummaries, studentLevel);

        // 레포트 엔티티 생성 및 저장
        LevelTestReport report = LevelTestReport.builder()
                .memberId(memberId)
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
                .conceptSummaries(conceptSummaries)
                .comprehensiveFeedback(savedReport.getComprehensiveFeedback())
                .nextLearningRecommendation(savedReport.getNextLearningRecommendation())
                .createdAt(savedReport.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportHistoryResponse> getReportHistory(Long memberId) {
        log.info("회원 {}의 레포트 히스토리 조회", memberId);

        List<LevelTestReport> reports = reportRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return reports.stream()
                .map(report -> ReportHistoryResponse.builder()
                        .reportId(report.getId())
                        .totalScore(report.getTotalScore())
                        .studentLevel(report.getStudentLevel())
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
                .conceptSummaries(conceptSummaries)
                .comprehensiveFeedback(report.getComprehensiveFeedback())
                .nextLearningRecommendation(report.getNextLearningRecommendation())
                .createdAt(report.getCreatedAt())
                .build();
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

    private List<ConceptAnalysis> getConceptSummariesFromAI(Long memberId) {
        try {
            String prompt = buildConceptAnalysisPrompt(memberId);
            String response = conceptAnalysisChatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            List<ConceptAnalysis> result = parseConceptAnalysisFromResponse(response);
            if (result.isEmpty()) {
                throw new RuntimeException("AI 응답이 비어있음");
            }
            return result;
        } catch (Exception e) {
            log.error("AI 개념 분석 실패, 폴백 방식 사용", e);
            return aggregateConceptAnalyses(memberAnswerRepository.findByMemberIdAndIsScored(memberId, true));
        }
    }

    private String buildConceptAnalysisPrompt(Long memberId) {
        List<MemberAnswer> answers = memberAnswerRepository.findByMemberIdAndIsScored(memberId, true);

        StringBuilder allConcepts = new StringBuilder();
        for (MemberAnswer answer : answers) {
            List<ConceptAnalysis> concepts = extractConceptAnalyses(answer);
            if (!concepts.isEmpty()) {
                allConcepts.append(String.format("[문제 %d] ", answers.indexOf(answer) + 1));
                for (ConceptAnalysis concept : concepts) {
                    allConcepts.append(String.format("%s(점수:%d, %s) ",
                            concept.getConceptName(), concept.getScore(), concept.getComment()));
                }
                allConcepts.append("\n");
            }
        }

        return String.format("""
            다음은 학습자의 각 문제별 개념 이해도 분석입니다:
            
            %s
            
            위 개념들을 분석하여 다음 작업을 수행해주세요:
            1. 의미가 중복되거나 유사한 개념들은 하나로 통합
            2. 가장 개선이 필요한(점수가 낮은) 6개 핵심 개념만 선별
            3. 각 개념의 평균 점수와 대표적인 코멘트 선택
            
            JSON 형식으로 정확히 6개만 응답:
            [
                {
                    "conceptName": "통합된 개념명",
                    "score": 평균점수(1-5 사이의 정수),
                    "comment": "대표 코멘트"
                }
            ]
            """, allConcepts.toString());
    }

    private List<ConceptAnalysis> parseConceptAnalysisFromResponse(String response) {
        try {
            String cleanedJson = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            TypeReference<List<ConceptAnalysis>> typeRef = new TypeReference<List<ConceptAnalysis>>() {};
            List<ConceptAnalysis> concepts = objectMapper.readValue(cleanedJson, typeRef);

            return concepts.stream().limit(6).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("개념 분석 응답 파싱 실패: {}", response, e);
            return new ArrayList<>();
        }
    }

    private List<ConceptAnalysis> aggregateConceptAnalyses(List<MemberAnswer> answers) {
        Map<String, List<ConceptAnalysis>> conceptMap = new HashMap<>();

        for (MemberAnswer answer : answers) {
            List<ConceptAnalysis> concepts = extractConceptAnalyses(answer);
            for (ConceptAnalysis concept : concepts) {
                conceptMap.computeIfAbsent(concept.getConceptName(), k -> new ArrayList<>())
                        .add(concept);
            }
        }

        List<ConceptAnalysis> result = new ArrayList<>();
        for (Map.Entry<String, List<ConceptAnalysis>> entry : conceptMap.entrySet()) {
            String conceptName = entry.getKey();
            List<ConceptAnalysis> conceptList = entry.getValue();

            double avgScore = conceptList.stream()
                    .mapToInt(ConceptAnalysis::getScore)
                    .average()
                    .orElse(1.0);

            int finalScore = (int) Math.round(avgScore);
            finalScore = Math.max(1, Math.min(5, finalScore));

            String representativeComment = conceptList.get(conceptList.size() - 1).getComment();

            result.add(ConceptAnalysis.builder()
                    .conceptName(conceptName)
                    .score(finalScore)
                    .comment(representativeComment)
                    .build());
        }

        return result.stream()
                .sorted(Comparator.comparingInt(ConceptAnalysis::getScore))
                .limit(6)
                .collect(Collectors.toList());
    }

    private List<ConceptAnalysis> extractConceptAnalyses(MemberAnswer answer) {
        if (answer.getConceptAnalyses() == null || answer.getConceptAnalyses().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            TypeReference<List<ConceptAnalysis>> typeRef = new TypeReference<List<ConceptAnalysis>>() {};
            return objectMapper.readValue(answer.getConceptAnalyses(), typeRef);
        } catch (Exception e) {
            log.error("Failed to parse concept analyses for answer ID: {}", answer.getId(), e);
            return new ArrayList<>();
        }
    }

    private String generateComprehensiveFeedback(Long memberId) {
        List<MemberAnswer> allAnswers = memberAnswerRepository.findByMemberIdAndIsScored(memberId, true);

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
            - 주요 강점 영역 1-2개
            - 가장 시급한 개선점 1-2개
            - 구체적인 학습 방향 제시
            
            ※ 마크다운 형식이나 소제목 없이 일반 텍스트로만 작성하세요.
            ※ 2-4문장으로 간결하게 작성하세요.
            ※ '이 학생은' 같은 3인칭 표현은 사용하지 말고, 주어를 생략하거나 직접적인 어조로 작성하세요.
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