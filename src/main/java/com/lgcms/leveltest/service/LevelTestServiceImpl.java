package com.lgcms.leveltest.service;

import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;
import com.lgcms.leveltest.repository.LevelTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LevelTestServiceImpl implements LevelTestService {

    private final LevelTestRepository levelTestRepository;

    @Override
    public LevelTestResponse createQuestion(LevelTestRequest request) {
        if (request.getCategory() == null || request.getDifficulty() == null) {
            throw new BaseException(LevelTestError.INVALID_REQUEST);
        }

        LevelTest levelTest = LevelTest.builder()
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .build();

        LevelTest saved = levelTestRepository.save(levelTest);

        return LevelTestResponse.builder()
                .id(saved.getId())
                .category(saved.getCategory())
                .difficulty(saved.getDifficulty())
                .question(saved.getQuestion())
                .answer(saved.getAnswer())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
