package com.lgcms.leveltest.dto.response.memberanswer;

import com.lgcms.leveltest.dto.response.scoring.ConceptAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportResponse {
    private final Integer totalScore;
    private final String studentLevel;
    private final List<ConceptAnalysis> conceptSummaries;
    private final String comprehensiveFeedback;
    private final String nextLearningRecommendation;
}
