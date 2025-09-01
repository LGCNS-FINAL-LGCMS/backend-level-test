package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.response.report.ReportDetailResponse;
import com.lgcms.leveltest.dto.response.report.ReportHistoryResponse;
import com.lgcms.leveltest.service.report.LevelTestReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/student/leveltest")
@RequiredArgsConstructor
public class LevelTestReportController {

    private final LevelTestReportService reportService;
    
    // 새 레포트 생성
    @PostMapping("/reports")
    public ResponseEntity<BaseResponse<ReportDetailResponse>> createReport(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return ResponseEntity.ok(BaseResponse.ok(reportService.createReport(memberId)));
    }

    // 특정 수강생의 레포트 히스토리 전체 목록 조회
    @GetMapping("/reports")
    public ResponseEntity<BaseResponse<List<ReportHistoryResponse>>> getReportHistory(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return ResponseEntity.ok(BaseResponse.ok(reportService.getReportHistory(memberId)));
    }

    // 특정 수강생의 특정 레포트 상세 조회
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<BaseResponse<ReportDetailResponse>> getReportDetail(
            @RequestHeader("X-USER-ID") @Valid Long memberId,
            @PathVariable @Valid Long reportId) {
        return ResponseEntity.ok(BaseResponse.ok(reportService.getReportDetail(memberId, reportId)));
    }
}