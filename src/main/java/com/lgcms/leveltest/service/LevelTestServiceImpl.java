package com.lgcms.leveltest.service;

import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.dto.request.leveltest.LevelTestRequest;
import com.lgcms.leveltest.dto.response.leveltest.LevelTestResponse;
import com.lgcms.leveltest.repository.LevelTestRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.lgcms.leveltest.repository.CategoryRedisRepository;
import com.lgcms.leveltest.dto.response.memberanswer.MemberQuestionResponse;
import com.lgcms.leveltest.domain.CategoryItem;
import com.lgcms.leveltest.domain.Difficulty;
import java.util.Collections;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelTestServiceImpl implements LevelTestService {

    private final LevelTestRepository levelTestRepository;
    private final CategoryRedisRepository categoryRedisRepository;
    private final CategoryMappingService categoryMappingService;
    private final QuestionRequestLogService questionRequestLogService;

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

        LevelTest updated = LevelTest.builder()
                .id(levelTest.getId())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .mustInclude(request.getMustInclude())
                .createdAt(levelTest.getCreatedAt())
                .build();

        LevelTest saved = levelTestRepository.save(updated);

        return LevelTestResponse.builder()
                .id(updated.getId())
                .category(updated.getCategory())
                .difficulty(updated.getDifficulty())
                .question(updated.getQuestion())
                .answer(updated.getAnswer())
                .mustInclude(updated.getMustInclude())
                .createdAt(updated.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
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

    @Override
    public List<MemberQuestionResponse> getQuestionsByCategory(Long categoryId) {
        log.info("카테고리 ID 목록으로 문제 생성: {}", categoryId);

        // Redis에서 카테고리 정보 조회
        List<CategoryItem> memberCategories = categoryRedisRepository.getCategoriesById(List.of(categoryId));
        log.info("레디스에서 조회된 카테고리: {}", memberCategories);

        // DB 카테고리로 매핑
        List<Category> dbCategories = categoryMappingService.mapToDbCategories(memberCategories);
        log.info("매핑된 DB 카테고리: {}", dbCategories);

        if (dbCategories.isEmpty()) {
            log.warn("매핑 가능한 카테고리가 없습니다. 전체 문제에서 랜덤 출제합니다.");
            return getRandomQuestions(10);
        }

        Category category = dbCategories.get(0);
        List<LevelTest> selectedQuestions = getQuestionsForCategory(category, 10);

        List<Long> questionIds = selectedQuestions.stream().map(LevelTest::getId).toList();
        Long memberId = getCurrentMemberId(); // 헤더에서 회원 ID 추출 (아래 메서드 추가 필요)
        questionRequestLogService.logQuestionRequest(memberId, questionIds);

        log.info("최종 선택된 문제 개수: {}", selectedQuestions.size());
        log.info("회원 {}가 요청한 문제 IDs: {}", memberId, questionIds);

        return selectedQuestions.stream()
                .map(this::convertToMemberQuestionResponse)
                .toList();
    }

    private List<LevelTest> getQuestionsForCategory(Category category, int count) {
        List<LevelTest> questions = new ArrayList<>();

        int lowCount = Math.max(1, (count * 3) / 10);
        int mediumCount = Math.max(1, (count * 4) / 10);
        int highCount = count - lowCount - mediumCount;

        questions.addAll(getRandomQuestionsFromCategoryAndDifficulty(category, Difficulty.LOW, lowCount));
        questions.addAll(getRandomQuestionsFromCategoryAndDifficulty(category, Difficulty.MEDIUM, mediumCount));
        questions.addAll(getRandomQuestionsFromCategoryAndDifficulty(category, Difficulty.HIGH, highCount));

        return questions;
    }

    private List<LevelTest> getRandomQuestionsFromCategoryAndDifficulty(Category category, Difficulty difficulty, int count) {
        List<LevelTest> questions = levelTestRepository.findByCategoryAndDifficulty(category, difficulty);
        Collections.shuffle(questions);
        return questions.stream().limit(count).collect(Collectors.toList());
    }

    private List<MemberQuestionResponse> getRandomQuestions(int count) {
        List<LevelTest> allQuestions = levelTestRepository.findAll();
        Collections.shuffle(allQuestions);
        return allQuestions.stream()
                .limit(count)
                .map(this::convertToMemberQuestionResponse)
                .toList();
    }

    private MemberQuestionResponse convertToMemberQuestionResponse(LevelTest levelTest) {
        return MemberQuestionResponse.builder()
                .id(levelTest.getId())
                .category(levelTest.getCategory())
                .difficulty(levelTest.getDifficulty())
                .question(levelTest.getQuestion())
                .build();
    }

    private Long getCurrentMemberId() {
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                            .getRequest();

            String userIdHeader = request.getHeader("X-USER-ID");
            if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
                throw new BaseException(LevelTestError.UNAUTHORIZED_ACCESS);
            }

            return Long.valueOf(userIdHeader);
        } catch (Exception e) {
            log.error("사용자 ID 추출 실패: {}", e.getMessage());
            throw new BaseException(LevelTestError.UNAUTHORIZED_ACCESS);
        }
    }

}
