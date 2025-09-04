package com.lgcms.leveltest.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestReportRequested {
    private long memberId;
    private long studentReportId;
}
