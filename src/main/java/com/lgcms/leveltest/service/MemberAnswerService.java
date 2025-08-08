package com.lgcms.leveltest.service;

import com.lgcms.leveltest.dto.request.memberanswer.MemberAnswerRequest;
import com.lgcms.leveltest.dto.response.memberanswer.MemberAnswerResponse;
import com.lgcms.leveltest.dto.response.memberanswer.MemberQuestionResponse;

import java.util.List;

public interface MemberAnswerService {
    // 사용자에게 문제 목록 제공 (답변하지 않은 문제만)
    List<MemberQuestionResponse> getUnansweredQuestions(Long memberId);

    // 답변 저장
    MemberAnswerResponse saveAnswer(Long memberId, MemberAnswerRequest request);

    // 사용자의 모든 답변 조회
    List<MemberAnswerResponse> getMemberAnswers(Long memberId);

    // 특정 답변 조회
    MemberAnswerResponse getMemberAnswer(Long memberId, Long answerId);
}