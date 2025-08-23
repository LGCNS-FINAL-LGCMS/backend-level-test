package com.lgcms.leveltest.dto.response.scoring;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConceptAnalysis {
    private String conceptName;
    private Integer score;
    private String comment;
}