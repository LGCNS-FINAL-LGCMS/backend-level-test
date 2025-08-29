package com.lgcms.leveltest.service.mamberanswer;

import com.lgcms.leveltest.dto.request.memberanswer.*;
import com.lgcms.leveltest.dto.response.memberanswer.*;

import java.util.List;

public interface MemberAnswerService {

    // 답변 조회
    List<MemberAnswerResponse> getMemberAnswers(Long memberId);

    // 새로운 일괄 제출 메서드
    void submitAllAnswers(Long memberId, MemberAnswerRequest request);
}