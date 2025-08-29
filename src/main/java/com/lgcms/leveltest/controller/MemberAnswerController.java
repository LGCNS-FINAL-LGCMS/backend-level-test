package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.request.memberanswer.*;
import com.lgcms.leveltest.dto.response.memberanswer.*;
import com.lgcms.leveltest.service.grading.GradingService;
import com.lgcms.leveltest.service.mamberanswer.MemberAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/student/leveltest")
@RequiredArgsConstructor
public class MemberAnswerController {

    private final MemberAnswerService memberAnswerService;
    private final GradingService gradingService;
    
    // 사용자의 모든 답변 조회
    @GetMapping("/answers")
    public ResponseEntity<BaseResponse<List<MemberAnswerResponse>>> getMemberAnswers(
            @RequestHeader("X-USER-ID") @Valid Long memberId) {
        return ResponseEntity.ok(BaseResponse.ok(memberAnswerService.getMemberAnswers(memberId)));
    }

    // 전체 문제에 대한 수강생 답변 일괄 제출
    @PostMapping("/answers/submit-all")
    public ResponseEntity<BaseResponse<String>> submitAllAnswers(
            @RequestHeader("X-USER-ID") @Valid Long memberId,
            @Valid @RequestBody MemberAnswerRequest request) {
        memberAnswerService.submitAllAnswers(memberId, request);
        return ResponseEntity.ok(BaseResponse.ok("답변이 성공적으로 제출되었습니다."));
    }
}