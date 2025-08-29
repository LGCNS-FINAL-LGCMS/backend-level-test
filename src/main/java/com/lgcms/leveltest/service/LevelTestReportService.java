package com.lgcms.leveltest.service;

import com.lgcms.leveltest.dto.response.report.ReportDetailResponse;
import com.lgcms.leveltest.dto.response.report.ReportHistoryResponse;

import java.util.List;

public interface LevelTestReportService {

    // 새 레포트 생성 및 저장
    ReportDetailResponse createReport(Long memberId);

    // 회원의 레포트 히스토리 조회
    List<ReportHistoryResponse> getReportHistory(Long memberId);

    // 특정 레포트 상세 조회
    ReportDetailResponse getReportDetail(Long memberId, Long reportId);
}