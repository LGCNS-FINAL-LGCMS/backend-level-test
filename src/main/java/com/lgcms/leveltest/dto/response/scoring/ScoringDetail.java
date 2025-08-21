package com.lgcms.leveltest.dto.response.scoring;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScoringDetail {
    private final String criterion;
    private final Integer points;
    private final Integer earnedPoints;
    private final String comment;
}