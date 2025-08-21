package com.lgcms.leveltest.service;

import com.lgcms.leveltest.dto.request.memberanswer.*;
import com.lgcms.leveltest.dto.response.memberanswer.*;

import java.util.List;

public interface MemberAnswerService {

    // 답변 조회
    List<MemberAnswerResponse> getMemberAnswers(Long memberId);

    // 새로운 일괄 제출 메서드
    SubmissionResponse submitAllAnswers(Long memberId, MemberAnswerRequest request);

    // 채점 진행 상황 조회
    GradingProgressResponse getGradingProgress(Long memberId);
}