package com.lgcms.leveltest.service;

import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;

import java.util.concurrent.CompletableFuture;

public interface GradingService {

    ScoringResult gradeAnswer(MemberAnswer memberAnswer);
    String generateComprehensiveFeedback(Long memberId);
}