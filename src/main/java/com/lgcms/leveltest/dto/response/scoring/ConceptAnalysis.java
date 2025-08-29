package com.lgcms.leveltest.dto.response.scoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptAnalysis {
    private String conceptName;
    private Integer score;
    private String comment;
}