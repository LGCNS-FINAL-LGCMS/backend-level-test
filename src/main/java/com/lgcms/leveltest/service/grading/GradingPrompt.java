package com.lgcms.leveltest.service.grading;

import com.lgcms.leveltest.domain.CategoryCriteria;
import com.lgcms.leveltest.domain.LevelTest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradingPrompt {

    private static final String SYSTEM_PROMPT = """
        당신은 프로그래밍 교육 전문가이자 공정한 채점관입니다.
        학생의 답변을 평가하고 건설적인 피드백을 제공해야 합니다.
        채점은 객관적이고 일관성 있게 진행하며, 답변의 정확성과 완성도를 종합적으로 평가합니다.
        """;

    private final LevelTest question;
    private final String memberAnswer;

    public String buildPrompt() {
        CategoryCriteria criteria = CategoryCriteria.valueOf(question.getCategory().name());
        CategoryCriteria.ScoringCriteria scoringCriteria = criteria.getScoringCriteria(question.getDifficulty());
        StringBuilder prompt = new StringBuilder();

        prompt.append("다음 문제에 대한 학생의 답변을 채점해주세요.\n\n");

        prompt.append("[문제 정보]\n");
        prompt.append("카테고리: ").append(question.getCategory().getCategoryName()).append("\n");
        prompt.append("난이도: ").append(question.getDifficulty().getDifficultyName()).append("\n");
        prompt.append("문제: ").append(question.getQuestion()).append("\n\n");

        prompt.append("[모범 답안]\n");
        prompt.append(question.getAnswer()).append("\n\n");

        prompt.append("[채점 기준]\n");
        if (question.getMustInclude() != null && !question.getMustInclude().isEmpty()) {
            prompt.append("필수 포함 키워드:\n");
            question.getMustInclude().forEach(keyword ->
                    prompt.append("  - ").append(keyword).append("\n"));
        }

        prompt.append("채점 가중치:\n");
        scoringCriteria.getWeights().forEach((key, value) ->
                prompt.append("  - ").append(key).append(": ").append(value).append("\n"));

        prompt.append("합격 기준: ").append(scoringCriteria.getPassThreshold()).append("점 이상\n");
        prompt.append("평가 방향: ").append(scoringCriteria.getGuideline()).append("\n\n");

        prompt.append("[학생 답변]\n");
        prompt.append(memberAnswer != null ? memberAnswer : "(답변 없음)").append("\n\n");

        prompt.append("[채점 요청]\n");
        prompt.append(getGradingInstructions());

        return prompt.toString();
    }

    private String getGradingInstructions() {
        return """
        위 정보를 바탕으로 다음 형식의 JSON으로 채점 결과를 제공해주세요:
        
        {
            "score": (0-100 사이의 정수),
            "isCorrect": (true/false - 70점 이상이면 true),
            "feedback": "전체적인 피드백",
            "strengths": "잘한 점 (없으면 '해당 없음')",
            "improvements": "개선할 점",
            "mustIncludeMatched": ["매칭된", "필수", "키워드"],
            "wrongKeywordsFound": ["발견된", "오답", "키워드"],
            "scoringDetails": [
                {
                    "criterion": "평가 항목",
                    "points": 배점,
                    "earnedPoints": 획득 점수,
                    "comment": "세부 코멘트"
                }
            ]
        }
        
        채점 시 주의사항:
        1. 필수 키워드가 포함되었는지 확인 (대소문자 구분 없음)
        2. 오답 키워드가 포함된 경우 감점
        3. 답변의 정확성, 완성도, 논리성을 종합 평가
        4. 부분 점수를 인정하여 세밀하게 채점
        5. 건설적이고 격려하는 톤으로 피드백 작성
        6. **strengths는 빈 문자열 금지 - 구체적 내용 또는 '해당 없음' 필수**
        
        **반드시 유효한 JSON 형식으로만 응답하세요.**
        """;
    }

    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }
}