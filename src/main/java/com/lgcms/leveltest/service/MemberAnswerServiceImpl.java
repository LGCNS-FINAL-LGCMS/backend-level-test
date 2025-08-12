package com.lgcms.leveltest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.request.memberanswer.MemberAnswerRequest;
import com.lgcms.leveltest.dto.response.memberanswer.MemberAnswerResponse;
import com.lgcms.leveltest.dto.response.memberanswer.MemberQuestionResponse;
import com.lgcms.leveltest.repository.LevelTestRepository;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
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

    @Override
    @Transactional(readOnly = true)
    public List<MemberQuestionResponse> getUnansweredQuestions(Long memberId) {
        List<LevelTest> allQuestions = levelTestRepository.findAll();
        List<Long> answeredQuestionIds = memberAnswerRepository.findAnsweredQuestionIdsByMemberId(memberId);

        return allQuestions.stream()
                .filter(q -> !answeredQuestionIds.contains(q.getId()))
                .map(this::convertToMemberQuestionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MemberAnswerResponse saveAnswer(Long memberId, MemberAnswerRequest request) {
        LevelTest question = levelTestRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BaseException(LevelTestError.QUESTION_NOT_FOUND));

        Optional<MemberAnswer> existingAnswer = memberAnswerRepository
                .findByMemberIdAndQuestionId(memberId, request.getQuestionId());

        MemberAnswer memberAnswer;
        if (existingAnswer.isPresent()) {
            memberAnswer = existingAnswer.get();
            memberAnswer.setMemberAnswer(request.getAnswer());
            // 답변이 수정되면 재채점이 필요하므로 채점 상태를 초기화 (필드 형식 맞춤)
            memberAnswer.setIsScored(false);
            memberAnswer.setScore(null);
            memberAnswer.setFeedback(null);
            memberAnswer.setIsCorrect(null);
            memberAnswer.setScoredAt(null);
        } else {
            memberAnswer = MemberAnswer.builder()
                    .memberId(memberId)
                    .question(question)
                    .memberAnswer(request.getAnswer())
                    .isScored(false)
                    .build();
        }

        MemberAnswer saved = memberAnswerRepository.save(memberAnswer);

        // 자동 채점 수행 (테스트 용 출력문)
        gradingService.gradeAnswerAsync(saved)
                .thenAccept(result -> {
                    System.out.println("Grading completed for answer ID: " + saved.getId() +
                            ", Score: " + result.getScore());
                })
                .exceptionally(ex -> {
                    System.err.println("Grading failed for answer ID: " + saved.getId() +
                            ", Error: " + ex.getMessage());
                    return null;
                });

        return convertToMemberAnswerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberAnswerResponse> getMemberAnswers(Long memberId) {
        List<MemberAnswer> answers = memberAnswerRepository.findByMemberId(memberId);
        return answers.stream()
                .map(this::convertToMemberAnswerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MemberAnswerResponse getMemberAnswer(Long memberId, Long answerId) {
        MemberAnswer memberAnswer = memberAnswerRepository.findById(answerId)
                .orElseThrow(() -> new BaseException(LevelTestError.ANSWER_NOT_FOUND));

        if (!memberAnswer.getMemberId().equals(memberId)) {
            throw new BaseException(LevelTestError.UNAUTHORIZED_ACCESS);
        }

        return convertToMemberAnswerResponse(memberAnswer);
    }

    private MemberQuestionResponse convertToMemberQuestionResponse(LevelTest levelTest) {
        return MemberQuestionResponse.builder()
                .id(levelTest.getId())
                .category(levelTest.getCategory())
                .difficulty(levelTest.getDifficulty())
                .question(levelTest.getQuestion())
                .build();
    }

    private MemberAnswerResponse convertToMemberAnswerResponse(MemberAnswer memberAnswer) {
        List<MemberAnswerResponse.ScoringDetail> scoringDetails = null;
        if (memberAnswer.getScoringDetails() != null && !memberAnswer.getScoringDetails().isEmpty()) {
            try {
                TypeReference<List<ScoringResult.ScoringDetail>> typeRef =
                        new TypeReference<List<ScoringResult.ScoringDetail>>() {};
                List<ScoringResult.ScoringDetail> details =
                        objectMapper.readValue(memberAnswer.getScoringDetails(), typeRef);

                scoringDetails = details.stream()
                        .map(detail -> MemberAnswerResponse.ScoringDetail.builder()
                                .criterion(detail.getCriterion())
                                .points(detail.getPoints())
                                .earnedPoints(detail.getEarnedPoints())
                                .comment(detail.getComment())
                                .build())
                        .toList();
            } catch (JsonProcessingException e) {
                log.error("Failed to parse scoring details", e);
            }
        }

        return MemberAnswerResponse.builder()
                .id(memberAnswer.getId())
                .memberId(memberAnswer.getMemberId())
                .questionId(memberAnswer.getQuestion().getId())
                .questionContent(memberAnswer.getQuestion().getQuestion())
                .memberAnswer(memberAnswer.getMemberAnswer())
                .createdAt(memberAnswer.getCreatedAt())
                .isScored(memberAnswer.getIsScored())
                .score(memberAnswer.getScore())
                .isCorrect(memberAnswer.getIsCorrect())
                .feedback(memberAnswer.getFeedback())
                .mustIncludeMatched(memberAnswer.getMustIncludeMatched())
                .scoredAt(memberAnswer.getScoredAt())
                .scoringModel(memberAnswer.getScoringModel())
                .scoringDetails(scoringDetails)
                .build();
    }
}