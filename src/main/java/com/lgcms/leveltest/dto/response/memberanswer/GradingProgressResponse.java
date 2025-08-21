package com.lgcms.leveltest.dto.response.memberanswer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradingProgressResponse {
    private Long totalQuestions;
    private Long completedGrading;
    private Double progressPercentage;
    private String status;
    private String message;
}