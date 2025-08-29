package com.lgcms.leveltest.dto.response.report;

import com.lgcms.leveltest.domain.StudentLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportHistoryResponse {
    private Long reportId;
    private Integer totalScore;
    private StudentLevel studentLevel;
    private LocalDateTime createdAt;
}