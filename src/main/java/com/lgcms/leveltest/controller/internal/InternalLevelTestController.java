package com.lgcms.leveltest.controller.internal;

import com.lgcms.leveltest.common.dto.BaseResponse;
import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;
import com.lgcms.leveltest.service.LevelTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/leveltest")
@RequiredArgsConstructor
public class InternalLevelTestController {

    private final LevelTestService levelTestService;

    @PostMapping("")
    public BaseResponse<LevelTestResponse> createQuestionInternal(@RequestBody LevelTestRequest request) {
        return BaseResponse.ok(levelTestService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public BaseResponse<LevelTestResponse> updateQuestionInternal(@PathVariable Long id,
                                                          @RequestBody LevelTestRequest request) {
        return BaseResponse.ok(levelTestService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteQuestionInternal(@PathVariable Long id) {
        levelTestService.deleteQuestion(id);
        return BaseResponse.ok("삭제 완료");
    }

}
