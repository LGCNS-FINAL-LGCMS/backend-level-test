package com.lgcms.leveltest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.Difficulty;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.request.memberanswer.*;
import com.lgcms.leveltest.dto.response.memberanswer.*;
import com.lgcms.leveltest.dto.response.scoring.ConceptAnalysis;
import com.lgcms.leveltest.dto.response.scoring.ScoringDetail;
import com.lgcms.leveltest.repository.LevelTestRepository;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import com.lgcms.leveltest.service.grading.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberAnswerServiceImpl implements MemberAnswerService {

    private final MemberAnswerRepository memberAnswerRepository;
    private final LevelTestRepository levelTestRepository;
    private final GradingService gradingService;
    private final ObjectMapper objectMapper;
    private final SequentialGradingService sequentialGradingService;
    private final QuestionRequestLogService questionRequestLogService;

    @Override
    @Transactional(readOnly = true)
    public List<MemberAnswerResponse> getMemberAnswers(Long memberId) {
        List<MemberAnswer> answers = memberAnswerRepository.findByMemberId(memberId);
        return answers.stream()
                .map(this::convertToMemberAnswerResponse)
                .collect(Collectors.toList());
    }

    private MemberAnswerResponse convertToMemberAnswerResponse(MemberAnswer memberAnswer) {
        if (memberAnswer.getScoringDetails() == null || memberAnswer.getScoringDetails().isEmpty()) {
            return buildMemberAnswerResponse(memberAnswer, null);
        }

        List<ScoringDetail> scoringDetails = null;
        try {
            TypeReference<List<ScoringDetail>> typeRef = new TypeReference<List<ScoringDetail>>() {};
            scoringDetails = objectMapper.readValue(memberAnswer.getScoringDetails(), typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse scoring details", e);
            return buildMemberAnswerResponse(memberAnswer, null);
        }

        return buildMemberAnswerResponse(memberAnswer, scoringDetails);
    }

    // 공통 응답 생성 메서드
    private MemberAnswerResponse buildMemberAnswerResponse(MemberAnswer memberAnswer,
                                                           List<ScoringDetail> scoringDetails) {
        return MemberAnswerResponse.builder()
                .id(memberAnswer.getId())
                .memberId(memberAnswer.getMemberId())
                .questionId(memberAnswer.getQuestion().getId())
                .questionContent(memberAnswer.getQuestion().getQuestion())
                .memberAnswer(memberAnswer.getMemberAnswer())
                .createdAt(memberAnswer.getCreatedAt())
                .isScored(memberAnswer.getIsScored())
                .score(memberAnswer.getScore())
                .feedback(memberAnswer.getFeedback())
                .mustIncludeMatched(memberAnswer.getMustIncludeMatched())
                .scoredAt(memberAnswer.getScoredAt())
                .scoringModel(memberAnswer.getScoringModel())
                .scoringDetails(scoringDetails)
                .build();
    }

    @Override
    @Transactional
    public void submitAllAnswers(Long memberId, MemberAnswerRequest request) {
        log.info("회원 {}의 답변 일괄 제출 시작. 문제 수: {}", memberId, request.getAnswers().size());

        List<Long> submittedQuestionIds = request.getAnswers().stream()
                .map(MemberAnswerRequest.AnswerItem::getQuestionId)
                .toList();

        boolean isAuthorized = questionRequestLogService.validateQuestionAccess(memberId, submittedQuestionIds);
        if (!isAuthorized) {
            throw new BaseException(LevelTestError.UNAUTHORIZED_QUESTION_ACCESS);
        }

        log.info("회원 {}의 문제 접근 권한 검증 통과", memberId);

        List<MemberAnswer> savedAnswers = new ArrayList<>();

        // 모든 답변 저장 (채점 상태는 false)
        for (MemberAnswerRequest.AnswerItem answerItem : request.getAnswers()) {
            LevelTest question = levelTestRepository.findById(answerItem.getQuestionId())
                    .orElseThrow(() -> new BaseException(LevelTestError.QUESTION_NOT_FOUND));

            MemberAnswer memberAnswer = MemberAnswer.builder()
                    .memberId(memberId)
                    .question(question)
                    .memberAnswer(answerItem.getAnswer())
                    .isScored(false)
                    .build();

            MemberAnswer saved = memberAnswerRepository.save(memberAnswer);
            savedAnswers.add(saved);
        }

        // 비동기로 순차 채점 시작
        sequentialGradingService.gradeAllAnswersSequentially(memberId, savedAnswers);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReport(Long memberId) {
        List<MemberAnswer> allAnswers = memberAnswerRepository.findByMemberIdAndIsScored(memberId, true);

        if (allAnswers.size() < 10) {
            throw new BaseException(LevelTestError.GRADING_NOT_COMPLETED);
        }

        // 1. 총점 계산 (난이도별 차등 배점)
        Integer totalScore = calculateTotalScore(allAnswers);

        // 2. 학생 레벨 결정
        String studentLevel = determineStudentLevel(totalScore);

        // 3. 개념별 이해도 취합
        List<ConceptAnalysis> conceptSummaries = aggregateConceptAnalyses(allAnswers);

        // 4. 종합 피드백 생성
        String comprehensiveFeedback = generateComprehensiveFeedbackText(allAnswers, totalScore, memberId);

        // 5. 다음 학습 추천 생성
        String nextLearningRecommendation = generateLearningRecommendation(conceptSummaries, studentLevel);

        return ReportResponse.builder()
                .totalScore(totalScore)
                .studentLevel(studentLevel)
                .conceptSummaries(conceptSummaries)
                .comprehensiveFeedback(comprehensiveFeedback)
                .nextLearningRecommendation(nextLearningRecommendation)
                .build();
    }

    private Integer calculateTotalScore(List<MemberAnswer> answers) {
        int totalScore = 0;
        for (MemberAnswer answer : answers) {
            int originalScore = answer.getScore() != null ? answer.getScore() : 0;
            Difficulty difficulty = answer.getQuestion().getDifficulty();

            // 난이도별 가중치 적용하여 100점 만점으로 변환
            int weightedScore = switch (difficulty) {
                case LOW -> (originalScore * 8) / 100;      // 8점 만점
                case MEDIUM -> (originalScore * 10) / 100;  // 10점 만점
                case HIGH -> (originalScore * 12) / 100;    // 12점 만점
            };
            totalScore += weightedScore;
        }
        return totalScore;
    }

    private String determineStudentLevel(Integer totalScore) {
        if (totalScore >= 90) return "상";
        if (totalScore >= 40) return "중";
        return "하";
    }

    private List<ConceptAnalysis> aggregateConceptAnalyses(List<MemberAnswer> answers) {
        Map<String, List<ConceptAnalysis>> conceptMap = new HashMap<>();

        // 각 답변에서 개념 분석 데이터 추출
        for (MemberAnswer answer : answers) {
            List<ConceptAnalysis> concepts = extractConceptAnalyses(answer);
            for (ConceptAnalysis concept : concepts) {
                conceptMap.computeIfAbsent(concept.getConceptName(), k -> new ArrayList<>())
                        .add(concept);
            }
        }

        // 개념별 평균 점수 계산 및 종합
        List<ConceptAnalysis> result = new ArrayList<>();
        for (Map.Entry<String, List<ConceptAnalysis>> entry : conceptMap.entrySet()) {
            String conceptName = entry.getKey();
            List<ConceptAnalysis> conceptList = entry.getValue();

            // 평균 점수 계산
            double avgScore = conceptList.stream()
                    .mapToInt(ConceptAnalysis::getScore)
                    .average()
                    .orElse(1.0);

            int finalScore = (int) Math.round(avgScore);
            finalScore = Math.max(1, Math.min(5, finalScore));

            // 대표 코멘트 선택 (가장 최근 것 또는 가장 긴 것)
            String representativeComment = conceptList.get(conceptList.size() - 1).getComment();

            result.add(ConceptAnalysis.builder()
                    .conceptName(conceptName)
                    .score(finalScore)
                    .comment(representativeComment)
                    .build());
        }

        return result;
    }

    private List<ConceptAnalysis> extractConceptAnalyses(MemberAnswer answer) {
        if (answer.getConceptAnalyses() == null || answer.getConceptAnalyses().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            TypeReference<List<ConceptAnalysis>> typeRef = new TypeReference<List<ConceptAnalysis>>() {};
            return objectMapper.readValue(answer.getConceptAnalyses(), typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse concept analyses for answer ID: {}", answer.getId(), e);
            return new ArrayList<>();
        }
    }

    private String generateComprehensiveFeedbackText(List<MemberAnswer> answers, Integer totalScore, Long memberId) {
        return gradingService.generateComprehensiveFeedback(memberId);
    }

    private String generateLearningRecommendation(List<ConceptAnalysis> conceptSummaries, String studentLevel) {
        StringBuilder recommendation = new StringBuilder();

        // 점수가 낮은 개념들 식별 (1-2점)
        List<ConceptAnalysis> weakConcepts = conceptSummaries.stream()
                .filter(concept -> concept.getScore() <= 2)
                .toList();

        if (!weakConcepts.isEmpty()) {
            recommendation.append("**우선 학습 권장 영역**: ");
            recommendation.append(weakConcepts.stream()
                    .map(ConceptAnalysis::getConceptName)
                    .collect(Collectors.joining(", ")));
            recommendation.append("\n\n");
        }

        // 레벨별 맞춤 추천
        switch (studentLevel) {
            case "상" -> {
                recommendation.append("**고급 과정 추천**\n");
                recommendation.append("- 실무 프로젝트 참여 및 포트폴리오 구축\n");
                recommendation.append("- 아키텍처 설계 및 성능 최적화 학습\n");
                recommendation.append("- 오픈소스 기여 및 기술 블로그 작성");
            }
            case "중" -> {
                recommendation.append("**중급 과정 추천**\n");
                recommendation.append("- 핵심 개념 보강 및 심화 학습\n");
                recommendation.append("- 실습 위주의 프로젝트 경험 쌓기\n");
                recommendation.append("- 코드 리뷰 및 협업 경험 늘리기");
            }
            case "하" -> {
                recommendation.append("**기초 과정 추천**\n");
                recommendation.append("- 기본 개념부터 차근차근 학습\n");
                recommendation.append("- 간단한 예제와 실습 반복하기\n");
                recommendation.append("- 멘토링이나 스터디 그룹 참여");
            }
        }

        return recommendation.toString();
    }
}