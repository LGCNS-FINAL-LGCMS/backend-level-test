package com.lgcms.leveltest.service;

import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;

public interface LevelTestService {
    LevelTestResponse createQuestion(LevelTestRequest request);
}
