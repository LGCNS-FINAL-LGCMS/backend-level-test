package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;
import com.lgcms.leveltest.dto.response.memberanswer.MemberQuestionResponse;
import com.lgcms.leveltest.repository.CategoryRedisRepository;
import com.lgcms.leveltest.service.leveltest.LevelTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/student/leveltest")
@RequiredArgsConstructor
public class LevelTestController {

    private final LevelTestService levelTestService;
    private final CategoryRedisRepository categoryRedisRepository;
    
    // 문제 추가
    @PostMapping("")
    public ResponseEntity<BaseResponse<LevelTestResponse>> createQuestion(@Valid @RequestBody LevelTestRequest request) {
        return ResponseEntity.ok(BaseResponse.ok(levelTestService.createQuestion(request)));
    }
    
    // 문제 수정
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<LevelTestResponse>> updateQuestion(@PathVariable @Valid Long id,
                                                          @Valid @RequestBody LevelTestRequest request) {
        return ResponseEntity.ok(BaseResponse.ok(levelTestService.updateQuestion(id, request)));
    }
    
    // 문제 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> deleteQuestion(@PathVariable @Valid Long id) {
        levelTestService.deleteQuestion(id);
        return ResponseEntity.ok(BaseResponse.ok("삭제 완료"));
    }
    
    // 특정 문제 조회
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<LevelTestResponse>> getQuestion(@PathVariable @Valid Long id) {
        return ResponseEntity.ok(BaseResponse.ok(levelTestService.getQuestion(id)));
    }

    // 전체 문제 조회
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<LevelTestResponse>>> getAllQuestions() {
        return ResponseEntity.ok(BaseResponse.ok(levelTestService.getAllQuestions()));
    }

    // 문제 출제
    @GetMapping("/questions/generate")
    public ResponseEntity<BaseResponse<List<MemberQuestionResponse>>> getQuestionsByCategories(
            @RequestHeader("X-USER-ID") @Valid Long memberId,
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(BaseResponse.ok(levelTestService.getQuestionsByCategory(categoryId)));
    }

}