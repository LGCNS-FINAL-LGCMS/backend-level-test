package com.lgcms.leveltest.service;

import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;

public interface LevelTestService {
    LevelTestResponse createQuestion(LevelTestRequest request);
    LevelTestResponse updateQuestion(Long id, LevelTestRequest request);
    void deleteQuestion(Long id);
}
