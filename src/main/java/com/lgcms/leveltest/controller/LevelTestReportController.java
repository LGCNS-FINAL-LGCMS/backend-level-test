package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.response.report.ReportDetailResponse;
import com.lgcms.leveltest.dto.response.report.ReportHistoryResponse;
import com.lgcms.leveltest.service.LevelTestReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/student/leveltest/reports")
@RequiredArgsConstructor
public class LevelTestReportController {

    private final LevelTestReportService reportService;
    
    // 새 레포트 생성
    @PostMapping("")
    public BaseResponse<ReportDetailResponse> createReport(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return BaseResponse.ok(reportService.createReport(memberId));
    }

    // 특정 수강생의 레포트 히스토리 전체 목록 조회
    @GetMapping("")
    public BaseResponse<List<ReportHistoryResponse>> getReportHistory(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return BaseResponse.ok(reportService.getReportHistory(memberId));
    }

    // 특정 수강생의 특정 레포트 상세 조회
    @GetMapping("/{reportId}")
    public BaseResponse<ReportDetailResponse> getReportDetail(
            @RequestHeader("X-USER-ID") @Valid Long memberId,
            @PathVariable @Valid Long reportId) {
        return BaseResponse.ok(reportService.getReportDetail(memberId, reportId));
    }
}