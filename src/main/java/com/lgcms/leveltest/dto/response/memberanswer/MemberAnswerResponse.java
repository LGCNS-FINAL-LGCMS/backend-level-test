package com.lgcms.leveltest.dto.response.memberanswer;

import com.lgcms.leveltest.dto.response.scoring.ScoringDetail;
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
    private String feedback;
    private String mustIncludeMatched;
    private LocalDateTime scoredAt;
    private String scoringModel;
    private List<ScoringDetail> scoringDetails;
}