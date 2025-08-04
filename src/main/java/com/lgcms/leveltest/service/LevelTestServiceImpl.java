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

    @Override
    public LevelTestResponse updateQuestion(Long id, LevelTestRequest request) {
        LevelTest levelTest = levelTestRepository.findById(id)
                .orElseThrow(() -> new BaseException(LevelTestError.QUESTION_NOT_FOUND));

        levelTest.setCategory(request.getCategory());
        levelTest.setDifficulty(request.getDifficulty());
        levelTest.setQuestion(request.getQuestion());
        levelTest.setAnswer(request.getAnswer());

        LevelTest updated = levelTestRepository.save(levelTest);

        return LevelTestResponse.builder()
                .id(updated.getId())
                .category(updated.getCategory())
                .difficulty(updated.getDifficulty())
                .question(updated.getQuestion())
                .answer(updated.getAnswer())
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteQuestion(Long id) {
        LevelTest levelTest = levelTestRepository.findById(id)
                .orElseThrow(() -> new BaseException(LevelTestError.QUESTION_NOT_FOUND));

        levelTestRepository.delete(levelTest);
    }

}
