package com.lgcms.leveltest.dto.response.memberanswer;

import com.lgcms.leveltest.dto.response.scoring.ConceptAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportResponse {
    private Integer totalScore;
    private String studentLevel;
    private List<ConceptAnalysis> conceptSummaries;
    private String comprehensiveFeedback;
    private String nextLearningRecommendation;
}
