package com.lgcms.leveltest.service;

import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;
import com.lgcms.leveltest.repository.LevelTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelTestServiceImpl implements LevelTestService {

    private final LevelTestRepository levelTestRepository;

    @Override
    public LevelTestResponse createQuestion(LevelTestRequest request) {

        LevelTest levelTest = LevelTest.builder()
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .mustInclude(request.getMustInclude())
                .build();

        LevelTest saved = levelTestRepository.save(levelTest);

        return LevelTestResponse.builder()
                .id(saved.getId())
                .category(saved.getCategory())
                .difficulty(saved.getDifficulty())
                .question(saved.getQuestion())
                .answer(saved.getAnswer())
                .mustInclude(saved.getMustInclude())
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
        levelTest.setMustInclude(request.getMustInclude());

        LevelTest updated = levelTestRepository.save(levelTest);

        return LevelTestResponse.builder()
                .id(updated.getId())
                .category(updated.getCategory())
                .difficulty(updated.getDifficulty())
                .question(updated.getQuestion())
                .answer(updated.getAnswer())
                .mustInclude(updated.getMustInclude())
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

    @Override
    public LevelTestResponse getQuestion(Long id) {
        LevelTest levelTest = levelTestRepository.findById(id)
                .orElseThrow(() -> new BaseException(LevelTestError.QUESTION_NOT_FOUND));

        return LevelTestResponse.builder()
                .id(levelTest.getId())
                .category(levelTest.getCategory())
                .difficulty(levelTest.getDifficulty())
                .question(levelTest.getQuestion())
                .answer(levelTest.getAnswer())
                .mustInclude(levelTest.getMustInclude())
                .createdAt(levelTest.getCreatedAt())
                .updatedAt(levelTest.getUpdatedAt())
                .build();
    }

    @Override
    public List<LevelTestResponse> getAllQuestions() {
        return levelTestRepository.findAll()
                .stream()
                .map(levelTest -> LevelTestResponse.builder()
                        .id(levelTest.getId())
                        .category(levelTest.getCategory())
                        .difficulty(levelTest.getDifficulty())
                        .question(levelTest.getQuestion())
                        .answer(levelTest.getAnswer())
                        .mustInclude(levelTest.getMustInclude())
                        .createdAt(levelTest.getCreatedAt())
                        .updatedAt(levelTest.getUpdatedAt())
                        .build())
                .toList();
    }

}
