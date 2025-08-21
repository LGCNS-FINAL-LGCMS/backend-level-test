package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.request.memberanswer.*;
import com.lgcms.leveltest.dto.response.memberanswer.*;
import com.lgcms.leveltest.service.GradingService;
import com.lgcms.leveltest.service.MemberAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/student/leveltest")
@RequiredArgsConstructor
public class MemberAnswerController {

    private final MemberAnswerService memberAnswerService;
    private final GradingService gradingService;

    @GetMapping("/answers")
    public BaseResponse<List<MemberAnswerResponse>> getMemberAnswers(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return BaseResponse.ok(memberAnswerService.getMemberAnswers(memberId));
    }

    @GetMapping("/answers/comprehensive-feedback")
    public BaseResponse<String> getComprehensiveFeedback(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return BaseResponse.ok(gradingService.generateComprehensiveFeedback(memberId));
    }

    // 새로운 일괄 제출 API
    @PostMapping("/answers/submit-all")
    public BaseResponse<SubmissionResponse> submitAllAnswers(
            @RequestHeader("X-USER-ID") @Valid Long memberId,
            @Valid @RequestBody MemberAnswerRequest request) {
        return BaseResponse.ok(memberAnswerService.submitAllAnswers(memberId, request));
    }

    // 채점 진행 상황 조회 API
    @GetMapping("/answers/progress")
    public BaseResponse<GradingProgressResponse> getGradingProgress(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return BaseResponse.ok(memberAnswerService.getGradingProgress(memberId));
    }
}