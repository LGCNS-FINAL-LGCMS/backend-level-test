package com.lgcms.leveltest.dto.response.scoring;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResult {

    private Integer studentScore;
    private String feedback;
    private List<String> mustIncludeMatched;
    private String strengths;
    private String improvements;
    private String modelUsed;
    private LocalDateTime scoredAt;
    private List<ScoringDetail> scoringDetails;
    private List<ConceptAnalysis> conceptAnalyses;
}