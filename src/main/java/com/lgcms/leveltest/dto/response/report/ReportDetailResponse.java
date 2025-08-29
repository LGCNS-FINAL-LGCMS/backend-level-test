package com.lgcms.leveltest.dto.response.report;

import com.lgcms.leveltest.domain.StudentLevel;
import com.lgcms.leveltest.dto.response.scoring.ConceptAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportDetailResponse {
    private Long reportId;
    private Long memberId;
    private Integer totalScore;
    private Integer totalQuestions;
    private StudentLevel studentLevel;
    private List<ConceptAnalysis> conceptSummaries;
    private String comprehensiveFeedback;
    private String nextLearningRecommendation;
    private LocalDateTime createdAt;
}