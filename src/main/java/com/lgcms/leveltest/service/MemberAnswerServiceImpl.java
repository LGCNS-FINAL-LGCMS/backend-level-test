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
import com.lgcms.leveltest.config.ChatClientConfig;
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
    private final ChatClientConfig chatClientConfig;

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

        // 1. 총점 계산
        Integer totalScore = calculateTotalScore(allAnswers);

        // 2. 학생 레벨 결정
        String studentLevel = determineStudentLevel(totalScore);

        // 3. AI를 통한 개념별 이해도 취합
        List<ConceptAnalysis> conceptSummaries = getConceptSummariesFromAI(memberId);

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

    private List<ConceptAnalysis> getConceptSummariesFromAI(Long memberId) {
        try {
            String prompt = buildConceptAnalysisPrompt(memberId);
            String response = chatClientConfig.getChatClient(0.2, 800)
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
            
            통합 예시:
            - "SpEL 기능", "SpEL 주요 용도" → "SpEL(Spring Expression Language)"
            - "Spring Data Commons", "Spring Data Commons 버전 관리" → "Spring Data Commons"
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

            // 정확히 6개로 제한
            return concepts.stream().limit(6).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("개념 분석 응답 파싱 실패: {}", response, e);
            // RuntimeException 대신 폴백을 위해 빈 리스트 반환
            return new ArrayList<>();
        }
    }

    // 폴백용 간단한 집계 메서드
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

        // 점수가 낮은 순으로 정렬하여 6개만 반환
        return result.stream()
                .sorted(Comparator.comparingInt(ConceptAnalysis::getScore))
                .limit(6)
                .collect(Collectors.toList());
    }

    private Integer calculateTotalScore(List<MemberAnswer> answers) {
        return answers.stream()
                .mapToInt(answer -> answer.getScore() != null ? answer.getScore() : 0)
                .sum();
    }

    private String determineStudentLevel(Integer totalScore) {
        if (totalScore >= 90) return "상";
        if (totalScore >= 40) return "중";
        return "하";
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
            recommendation.append("우선 학습이 필요한 영역은 ");
            recommendation.append(weakConcepts.stream()
                    .map(ConceptAnalysis::getConceptName)
                    .collect(Collectors.joining(", ")));
            recommendation.append("입니다. ");
        }

        // 레벨별 맞춤 추천
        switch (studentLevel) {
            case "상" -> {
                recommendation.append("고급 과정에서는 실무 프로젝트 참여와 포트폴리오 구축을 통해 역량을 확장하고, 아키텍처 설계와 성능 최적화 학습에 집중하며, 오픈소스 기여나 기술 블로그 작성을 통해 경험을 넓혀가는 것이 좋습니다.");
            }
            case "중" -> {
                recommendation.append("중급 수준에서는 핵심 개념을 보강하고 심화 학습을 진행하며, 실습 위주의 프로젝트 경험을 쌓고 코드 리뷰와 협업 경험을 늘려가는 것이 도움이 됩니다.");
            }
            case "하" -> {
                recommendation.append("기초 단계에서는 기본 개념을 차근차근 학습하고 간단한 예제와 실습을 반복하며, 멘토링이나 스터디 그룹에 참여하여 학습 동기를 유지하는 것이 좋습니다.");
            }
        }

        return recommendation.toString();
    }
}