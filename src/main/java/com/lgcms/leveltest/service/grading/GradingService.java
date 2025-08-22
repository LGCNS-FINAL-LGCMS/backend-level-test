package com.lgcms.leveltest.service.grading;

import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;

public interface GradingService {

    ScoringResult gradeAnswer(MemberAnswer memberAnswer);
    String generateComprehensiveFeedback(Long memberId);
}