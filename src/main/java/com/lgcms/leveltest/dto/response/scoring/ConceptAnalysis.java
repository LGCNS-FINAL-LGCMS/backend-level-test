package com.lgcms.leveltest.dto.response.scoring;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConceptAnalysis {
    private final String conceptName;
    private final Integer understanding;
    private final String comment;
}