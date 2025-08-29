package com.lgcms.leveltest.service.mamberanswer;

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
import com.lgcms.leveltest.service.grading.GradingService;
import com.lgcms.leveltest.service.grading.SequentialGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
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
    private final ChatClient conceptAnalysisChatClient;

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
}