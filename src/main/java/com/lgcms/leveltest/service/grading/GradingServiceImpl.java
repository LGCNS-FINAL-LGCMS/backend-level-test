package com.lgcms.leveltest.service.grading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.common.dto.exception.BaseException;
import com.lgcms.leveltest.common.dto.exception.LevelTestError;
import com.lgcms.leveltest.config.ChatClientConfig;
import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import com.lgcms.leveltest.service.MemberAnswerUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    private final MemberAnswerRepository memberAnswerRepository;
    private final ObjectMapper objectMapper;
    private final MemberAnswerUpdateService memberAnswerUpdateService;
    private final ChatClientConfig chatClientConfig;

    @Override
    @Transactional
    public ScoringResult gradeAnswer(MemberAnswer memberAnswer) {
        log.info("Starting grading for answer ID: {}", memberAnswer.getId());

        try {
            String promptText = GradingPrompt.buildPrompt(
                    memberAnswer.getQuestion(),
                    memberAnswer.getMemberAnswer()
            );

            String responseContent = chatClientConfig.getChatClient(0.1, 2000)
                    .prompt()
                    .system(GradingPrompt.getSystemPrompt())
                    .user(promptText)
                    .call()
                    .content();

            ScoringResult result = parseGradingResponse(responseContent);
            memberAnswerUpdateService.updateWithScoringResult(memberAnswer, result);

            log.info("Grading completed for answer ID: {}. Score: {}",
                    memberAnswer.getId(), result.getScore());
            return result;

        } catch (Exception e) {
            log.error("Error during grading for answer ID: {}", memberAnswer.getId(), e);
            throw new BaseException(LevelTestError.GRADING_FAILED);
        }
    }

    @Override
    public String generateComprehensiveFeedback(Long memberId) {
        List<MemberAnswer> allAnswers = memberAnswerRepository.findByMemberIdAndIsScored(memberId, true);

        if (allAnswers.size() < 10) {
            return "아직 모든 문제의 채점이 완료되지 않았습니다. (" + allAnswers.size() + "/10)";
        }

        try {
            String promptText = GradingPrompt.buildComprehensiveFeedbackPrompt(allAnswers);

            return chatClientConfig.getChatClient(0.3, 1500)
                    .prompt()
                    .user(promptText)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Error generating comprehensive feedback for member: {}", memberId, e);
            return "종합 피드백 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private ScoringResult parseGradingResponse(String jsonResponse) {
        try {
            String cleanedJson = jsonResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .replaceAll("```", "")
                    .trim();

            ScoringResult result = objectMapper.readValue(cleanedJson, ScoringResult.class);

            log.info("=== PARSED RESULT ===");
            log.info("Score: {}", result.getScore());
            log.info("Feedback: {}", result.getFeedback());
            log.info("Strengths: {}", result.getStrengths());
            log.info("Improvements: {}", result.getImprovements());
            log.info("=== PARSED RESULT END ===");

            return result;

        } catch (Exception e) {
            log.error("Failed to parse grading response. Original response: [{}]", jsonResponse);
            log.error("Error details: ", e);

            return ScoringResult.builder()
                    .score(75)
                    .feedback("AI 채점 응답 파싱에 실패했습니다. 답변 내용: " +
                            (jsonResponse.length() > 100 ?
                                    jsonResponse.substring(0, 100) + "..." : jsonResponse))
                    .strengths("시스템 오류로 자동 분석 불가")
                    .improvements("수동 검토가 필요합니다")
                    .mustIncludeMatched(new ArrayList<>())
                    .scoringDetails(new ArrayList<>())
                    .modelUsed("anthropic.claude-3-haiku-20240307-v1:0")
                    .scoredAt(LocalDateTime.now())
                    .build();
        }
    }
}