package com.lgcms.leveltest.controller;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;
import com.lgcms.leveltest.service.LevelTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leveltest")
@RequiredArgsConstructor
public class LevelTestController {

    private final LevelTestService levelTestService;

    @PostMapping("")
    public BaseResponse<LevelTestResponse> createQuestion(@RequestBody LevelTestRequest request) {
        return BaseResponse.ok(levelTestService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public BaseResponse<LevelTestResponse> updateQuestion(@PathVariable Long id,
                                                          @RequestBody LevelTestRequest request) {
        return BaseResponse.ok(levelTestService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteQuestion(@PathVariable Long id) {
        levelTestService.deleteQuestion(id);
        return BaseResponse.ok("삭제 완료");
    }

    @GetMapping("/{id}")
    public BaseResponse<LevelTestResponse> getQuestion(@PathVariable Long id) {
        return BaseResponse.ok(levelTestService.getQuestion(id));
    }

    @GetMapping("")
    public BaseResponse<List<LevelTestResponse>> getAllQuestions() {
        return BaseResponse.ok(levelTestService.getAllQuestions());
    }


}