package com.lgcms.leveltest.dto.response.report;

import com.lgcms.leveltest.domain.StudentLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistoryResponse {
    private Long reportId;
    private Integer totalScore;
    private StudentLevel studentLevel;
    private LocalDateTime createdAt;
}