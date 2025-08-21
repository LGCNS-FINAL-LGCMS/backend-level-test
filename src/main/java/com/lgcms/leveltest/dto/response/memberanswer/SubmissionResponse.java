package com.lgcms.leveltest.dto.response.memberanswer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubmissionResponse {
    private final String message;
    private final String status;
    private final String estimatedTime;
    private final Integer totalQuestions;
}