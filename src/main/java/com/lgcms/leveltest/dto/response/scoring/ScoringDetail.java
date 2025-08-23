package com.lgcms.leveltest.dto.response.scoring;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScoringDetail {
    private String criterion;
    private Integer points;
    private Integer earnedPoints;
    private String comment;
}