package com.lgcms.leveltest.service;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberAnswerServiceImpl implements MemberAnswerService {

    private final MemberAnswerRepository memberAnswerRepository;
    private final LevelTestRepository levelTestRepository;

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
        } else {
            memberAnswer = MemberAnswer.builder()
                    .memberId(memberId)
                    .question(question)
                    .memberAnswer(request.getAnswer())
                    .build();
        }

        MemberAnswer saved = memberAnswerRepository.save(memberAnswer);
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
        return MemberAnswerResponse.builder()
                .id(memberAnswer.getId())
                .memberId(memberAnswer.getMemberId())
                .questionId(memberAnswer.getQuestion().getId())
                .questionContent(memberAnswer.getQuestion().getQuestion())
                .memberAnswer(memberAnswer.getMemberAnswer())
                .createdAt(memberAnswer.getCreatedAt())
                .build();
    }
}