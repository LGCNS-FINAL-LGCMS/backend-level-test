package com.lgcms.leveltest.service;

import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;

import java.util.concurrent.CompletableFuture;

public interface GradingService {

    ScoringResult gradeAnswer(MemberAnswer memberAnswer);

    CompletableFuture<ScoringResult> gradeAnswerAsync(MemberAnswer memberAnswer);

    // 답변 ID로 재채점
    ScoringResult regradeAnswer(Long answerId);

    int gradeAllUnscoredAnswers(Long memberId);
}