package com.lgcms.leveltest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.request.memberanswer.*;
import com.lgcms.leveltest.dto.response.memberanswer.*;
import com.lgcms.leveltest.dto.response.scoring.ScoringDetail;
import com.lgcms.leveltest.repository.LevelTestRepository;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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
    private static AtomicLong lastGradingTime = new AtomicLong(0);
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

    private MemberQuestionResponse convertToMemberQuestionResponse(LevelTest levelTest) {
        return MemberQuestionResponse.builder()
                .id(levelTest.getId())
                .category(levelTest.getCategory())
                .difficulty(levelTest.getDifficulty())
                .question(levelTest.getQuestion())
                .build();
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
    public SubmissionResponse submitAllAnswers(Long memberId, MemberAnswerRequest request) {
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

        return SubmissionResponse.builder()
                .message("모든 답변이 제출되었습니다. 채점이 진행 중입니다.")
                .status("GRADING_IN_PROGRESS")
                .estimatedTime("약 " + (request.getAnswers().size() * 3 / 60 + 1) + "분 소요 예정")
                .totalQuestions(request.getAnswers().size())
                .build();
    }

    // 채점 진행 상황 조회 메서드 추가
    @Override
    @Transactional(readOnly = true)
    public GradingProgressResponse getGradingProgress(Long memberId) {
        long totalQuestions = memberAnswerRepository.countByMemberId(memberId);
        long completedGrading = memberAnswerRepository.countScoredAnswersByMemberId(memberId);

        double progressPercentage = totalQuestions > 0 ?
                (completedGrading * 100.0) / totalQuestions : 0.0;

        String status = (totalQuestions > 0 && completedGrading == totalQuestions) ?
                "COMPLETED" : "IN_PROGRESS";

        String message = status.equals("COMPLETED") ?
                "모든 문제의 채점이 완료되었습니다" :
                String.format("채점 진행 중 (%d/%d)", completedGrading, totalQuestions);

        return GradingProgressResponse.builder()
                .totalQuestions(totalQuestions)
                .completedGrading(completedGrading)
                .progressPercentage(progressPercentage)
                .status(status)
                .message(message)
                .build();
    }
}