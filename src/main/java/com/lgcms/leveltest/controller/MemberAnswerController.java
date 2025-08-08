package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.request.memberanswer.MemberAnswerRequest;
import com.lgcms.leveltest.dto.response.memberanswer.MemberAnswerResponse;
import com.lgcms.leveltest.dto.response.memberanswer.MemberQuestionResponse;
import com.lgcms.leveltest.service.MemberAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leveltest/member")
@RequiredArgsConstructor
public class MemberAnswerController {

    private final MemberAnswerService memberAnswerService;

    @GetMapping("/questions")
    public BaseResponse<List<MemberQuestionResponse>> getUnansweredQuestions(
            @RequestHeader("X-USER-ID") Long memberId) {
        return BaseResponse.ok(memberAnswerService.getUnansweredQuestions(memberId));
    }

    @PostMapping("/answers")
    public BaseResponse<MemberAnswerResponse> saveAnswer(
            @RequestHeader("X-USER-ID") Long memberId,
            @Valid @RequestBody MemberAnswerRequest request) {
        return BaseResponse.ok(memberAnswerService.saveAnswer(memberId, request));
    }

    @GetMapping("/answers")
    public BaseResponse<List<MemberAnswerResponse>> getMemberAnswers(
            @RequestHeader("X-USER-ID") Long memberId) {
        return BaseResponse.ok(memberAnswerService.getMemberAnswers(memberId));
    }

    @GetMapping("/answers/{answerId}")
    public BaseResponse<MemberAnswerResponse> getMemberAnswer(
            @RequestHeader("X-USER-ID") Long memberId,
            @PathVariable Long answerId) {
        return BaseResponse.ok(memberAnswerService.getMemberAnswer(memberId, answerId));
    }
}