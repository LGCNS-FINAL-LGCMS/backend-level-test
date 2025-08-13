package com.lgcms.leveltest.dto.response.scoring;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringResult {

    private Integer score;
    private Boolean isCorrect;
    private String feedback;
    private List<String> mustIncludeMatched;
    private List<String> wrongKeywordsFound;
    private String strengths;
    private String improvements;
    private String modelUsed;
    private LocalDateTime scoredAt;
    private List<ScoringDetail> scoringDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoringDetail {
        private String criterion;
        private Integer points;
        private Integer earnedPoints;
        private String comment;
    }
}