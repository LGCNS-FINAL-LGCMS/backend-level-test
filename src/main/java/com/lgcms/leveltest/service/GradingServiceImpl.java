package com.lgcms.leveltest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import com.lgcms.leveltest.service.grading.GradingPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    private final ChatClient.Builder chatClientBuilder;
    private final MemberAnswerRepository memberAnswerRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ScoringResult gradeAnswer(MemberAnswer memberAnswer) {
        log.info("Starting grading for answer ID: {}", memberAnswer.getId());

        try {
            // 모델 명시
            ChatOptions chatOptions = ChatOptions.builder()
                    .model("anthropic.claude-3-haiku-20240307-v1:0")
                    .maxTokens(2000)
                    .temperature(0.1)
                    .build();

            ChatClient chatClient = chatClientBuilder
                    .defaultOptions(chatOptions)
                    .build();

            // 프롬프트 생성
            GradingPrompt gradingPrompt = GradingPrompt.builder()
                    .question(memberAnswer.getQuestion())
                    .memberAnswer(memberAnswer.getMemberAnswer())
                    .build();

            String promptText = gradingPrompt.buildPrompt();

            log.debug("Sending prompt to Claude Haiku: {}", promptText);
            
            String responseContent = chatClient.prompt()
                    .system(GradingPrompt.getSystemPrompt())
                    .user(promptText)
                    .call()
                    .content();

            log.debug("Received response from Claude: {}", responseContent);
            ScoringResult result = parseGradingResponse(responseContent);

            // 채점 결과 저장
            updateMemberAnswer(memberAnswer, result);

            log.info("Grading completed for answer ID: {}. Score: {}",
                    memberAnswer.getId(), result.getScore());

            return result;

        } catch (Exception e) {
            log.error("Error during grading for answer ID: {}", memberAnswer.getId(), e);
            throw new BaseException(LevelTestError.GRADING_FAILED);
        }
    }

    private void updateMemberAnswer(MemberAnswer memberAnswer, ScoringResult result) {
        memberAnswer.setScore(result.getScore());
        memberAnswer.setIsCorrect(result.getIsCorrect());
        memberAnswer.setFeedback(result.getFeedback());
        memberAnswer.setIsScored(true);
        memberAnswer.setScoredAt(LocalDateTime.now());
        memberAnswer.setScoringModel("anthropic.claude-3-haiku-20240307-v1:0"); // 하드코딩

        if (result.getMustIncludeMatched() != null) {
            memberAnswer.setMustIncludeMatched(
                    String.join(",", result.getMustIncludeMatched())
            );
        }

        // 채점 상세 정보를 JSON으로 저장
        try {
            if (result.getScoringDetails() != null) {
                String detailsJson = objectMapper.writeValueAsString(result.getScoringDetails());
                memberAnswer.setScoringDetails(detailsJson);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize scoring details", e);
        }

        memberAnswerRepository.save(memberAnswer);
        result.setScoredAt(memberAnswer.getScoredAt());
        result.setModelUsed("anthropic.claude-3-haiku-20240307-v1:0");
    }

    @Override
    @Async("gradingExecutor")
    public CompletableFuture<ScoringResult> gradeAnswerAsync(MemberAnswer memberAnswer) {
        return CompletableFuture.supplyAsync(() -> gradeAnswer(memberAnswer));
    }

    @Override
    @Transactional
    public ScoringResult regradeAnswer(Long answerId) {
        MemberAnswer memberAnswer = memberAnswerRepository.findById(answerId)
                .orElseThrow(() -> new BaseException(LevelTestError.ANSWER_NOT_FOUND));

        log.info("Regrading answer ID: {}", answerId);

        memberAnswer.setIsScored(false);
        memberAnswer.setScore(null);
        memberAnswer.setFeedback(null);
        memberAnswerRepository.save(memberAnswer);

        return gradeAnswer(memberAnswer);
    }

    @Override
    @Transactional
    public int gradeAllUnscoredAnswers(Long memberId) {
        List<MemberAnswer> unscoredAnswers = memberAnswerRepository
                .findByMemberIdAndIsScored(memberId, false);

        log.info("Found {} unscored answers for member ID: {}",
                unscoredAnswers.size(), memberId);

        int gradedCount = 0;
        for (MemberAnswer answer : unscoredAnswers) {
            try {
                gradeAnswer(answer);
                gradedCount++;
            } catch (Exception e) {
                log.error("Failed to grade answer ID: {}", answer.getId(), e);
            }
        }

        log.info("Graded {} out of {} answers for member ID: {}",
                gradedCount, unscoredAnswers.size(), memberId);

        return gradedCount;
    }

    private ScoringResult parseGradingResponse(String jsonResponse) {
        try {
            log.info("=== CLAUDE RAW RESPONSE ===");
            log.info(jsonResponse);
            log.info("=== CLAUDE RAW RESPONSE END ===");

            String cleanedJson = jsonResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .replaceAll("```", "")
                    .trim();

            log.info("=== CLEANED JSON ===");
            log.info(cleanedJson);
            log.info("=== CLEANED JSON END ===");

            ScoringResult result = objectMapper.readValue(cleanedJson, ScoringResult.class);

            log.info("=== PARSED RESULT ===");
            log.info("Score: {}", result.getScore());
            log.info("IsCorrect: {}", result.getIsCorrect());
            log.info("Feedback: {}", result.getFeedback());
            log.info("Strengths: {}", result.getStrengths());
            log.info("Improvements: {}", result.getImprovements());
            log.info("MustIncludeMatched: {}", result.getMustIncludeMatched());
            log.info("WrongKeywordsFound: {}", result.getWrongKeywordsFound());
            log.info("=== PARSED RESULT END ===");

            return result;

        } catch (Exception e) {
            log.error("Failed to parse grading response. Original response: [{}]", jsonResponse);
            log.error("Error details: ", e);

            return ScoringResult.builder()
                    .score(75)
                    .isCorrect(true)
                    .feedback("AI 채점 응답 파싱에 실패했습니다. 답변 내용: " +
                            (jsonResponse.length() > 100 ?
                                    jsonResponse.substring(0, 100) + "..." : jsonResponse))
                    .strengths("시스템 오류로 자동 분석 불가")
                    .improvements("수동 검토가 필요합니다")
                    .mustIncludeMatched(new ArrayList<>())
                    .wrongKeywordsFound(new ArrayList<>())
                    .scoringDetails(new ArrayList<>())
                    .modelUsed("anthropic.claude-3-haiku-20240307-v1:0")
                    .scoredAt(LocalDateTime.now())
                    .build();
        }
    }
}