package com.lgcms.leveltest.dto.response.memberanswer;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemberAnswerResponse {
    private Long id;
    private Long memberId;
    private Long questionId;
    private String questionContent;
    private String memberAnswer;
    private LocalDateTime createdAt;

    private Boolean isScored;
    private Integer score;
    private Boolean isCorrect;
    private String feedback;
    private String mustIncludeMatched;
    private String wrongKeywordsFound;
    private LocalDateTime scoredAt;
    private String scoringModel;
    private List<ScoringDetail> scoringDetails;  // List<ScoringDetail>로 변경

    @Getter
    @Builder
    public static class ScoringDetail {
        private String criterion;
        private Integer points;
        private Integer earnedPoints;
        private String comment;
    }
}