package com.lgcms.leveltest.service;

import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;

import java.util.List;

public interface LevelTestService {
    LevelTestResponse createQuestion(LevelTestRequest request);
    LevelTestResponse updateQuestion(Long id, LevelTestRequest request);
    void deleteQuestion(Long id);

    LevelTestResponse getQuestion(Long id);
    List<LevelTestResponse> getAllQuestions();
}
