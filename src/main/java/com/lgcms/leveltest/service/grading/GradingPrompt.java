package com.lgcms.leveltest.service.grading;

import com.lgcms.leveltest.domain.CategoryCriteria;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.domain.MemberAnswer;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradingPrompt {

    private static final String SYSTEM_PROMPT = """
        당신은 프로그래밍 교육 전문가이자 공정한 채점관입니다.
        학생의 개념 이해도를 정확히 평가하고 성장을 돕는 건설적인 피드백을 제공해야 합니다.
        
        [채점 철학]
        - 개념 이해도 중심: 학생이 해당 주제의 핵심 개념을 얼마나 정확히 이해했는지가 가장 중요
        - 표현의 관대함: 개념을 올바르게 이해했다면 서툰 표현이어도 적절한 점수 부여
        - 부분 점수 인정: 완전하지 않아도 올바른 방향의 이해를 보이면 부분 점수 부여
        - 성장 지향적 평가: 틀린 부분보다 이해한 부분을 먼저 인정하고 발전 방향 제시
        - 점수 기반 평가: 합격/불합격이 아닌 0-100점 범위의 점수로 이해도 측정
        
        이 레벨 테스트는 수강생의 현재 실력을 파악하여 적절한 학습 방향을 제시하는 것이 목적입니다.
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
            prompt.append("핵심 키워드 (포함 권장):\n");
            question.getMustInclude().forEach(keyword ->
                    prompt.append("  - ").append(keyword).append("\n"));
            prompt.append("※ 키워드가 없어도 개념을 올바르게 설명했다면 적절한 점수 부여\n\n");
        }

        prompt.append("평가 가중치:\n");
        scoringCriteria.getWeights().forEach((key, value) ->
                prompt.append("  - ").append(key).append(": ").append((int)(value * 100)).append("%\n"));

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
            "feedback": "전체적인 피드백 (학생의 이해도 수준과 개선 방향)",
            "strengths": "잘한 점 또는 올바르게 이해한 부분 (없으면 '기본 개념 학습 필요')",
            "improvements": "개선할 점 및 추가 학습이 필요한 영역",
            "mustIncludeMatched": ["언급된", "핵심", "키워드"],
            "scoringDetails": [
                {
                    "criterion": "평가 항목",
                    "points": 배점,
                    "earnedPoints": 획득 점수,
                    "comment": "세부 평가 내용"
                }
            ]
        }
        
        채점 가이드라인:
        1. 개념 이해도를 가장 중요하게 평가 (핵심 내용의 정확성)
        2. 핵심 키워드 포함 여부 확인하되, 키워드가 없어도 올바른 설명이면 인정
        3. 부분적 이해도 적절히 점수로 반영 (0점, 100점 극단 지양)
        4. 답변이 없거나 완전히 틀린 경우에만 낮은 점수 부여
        5. 건설적이고 격려하는 톤으로 피드백 작성
        6. strengths는 구체적 내용 작성 필수 (빈 문자열 금지)
        7. 합격/불합격 판정하지 않고 점수로만 평가
        
        점수 기준:
        - 90-100점: 매우 정확하고 완전한 이해
        - 80-89점: 핵심 개념 이해, 일부 세부사항 부족
        - 70-79점: 기본 개념 이해, 설명 미흡하거나 일부 오류
        - 60-69점: 부분적 이해, 중요한 개념 누락 또는 오해
        - 50-59점: 최소한의 이해, 많은 부분 부정확
        - 40점 이하: 답변 없음 또는 완전히 잘못된 이해
        
        **반드시 유효한 JSON 형식으로만 응답하세요.**
        """;
    }

    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildComprehensiveFeedbackPrompt(List<MemberAnswer> answers) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("다음은 학생의 프로그래밍 레벨테스트 전체 답변입니다. 종합적인 피드백을 제공해주세요.\n\n");

        // 전체 점수 계산
        double avgScore = answers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).average().orElse(0.0);
        int totalQuestions = answers.size();

        prompt.append(String.format("[전체 성과] 평균 점수: %.1f점 (총 %d문제)\n\n", avgScore, totalQuestions));

        for (int i = 0; i < answers.size(); i++) {
            MemberAnswer answer = answers.get(i);
            prompt.append(String.format("[문제 %d] (%s - %s) %s\n",
                    i+1,
                    answer.getQuestion().getCategory().getCategoryName(),
                    answer.getQuestion().getDifficulty().getDifficultyName(),
                    answer.getQuestion().getQuestion()));
            prompt.append(String.format("[학생답변] %s\n", answer.getMemberAnswer()));
            prompt.append(String.format("[점수] %d점\n\n", answer.getScore() != null ? answer.getScore() : 0));
        }

        prompt.append("다음 형식으로 종합 피드백을 제공해주세요:\n\n");
        prompt.append("**전체 실력 분석**\n");
        prompt.append("- 평균 점수를 바탕으로 한 현재 실력 수준 평가\n");
        prompt.append("- 전반적인 개념 이해도와 학습 상태\n\n");
        prompt.append("**강점 영역**\n");
        prompt.append("- 잘 이해하고 있는 개념들과 높은 점수를 받은 영역\n");
        prompt.append("- 앞으로 더 발전시킬 수 있는 부분\n\n");
        prompt.append("**개선이 필요한 영역**\n");
        prompt.append("- 낮은 점수를 받은 영역과 부족한 개념들\n");
        prompt.append("- 우선적으로 학습해야 할 주제들\n\n");
        prompt.append("**맞춤 학습 가이드**\n");
        prompt.append("- 현재 수준에 맞는 구체적인 학습 방향\n");
        prompt.append("- 단계별 학습 추천 사항\n\n");
        prompt.append("**실력 향상을 위한 조언**\n");
        prompt.append("- 효과적인 학습 방법과 실습 방향\n");
        prompt.append("- 다음 레벨로 올라가기 위한 구체적인 액션 플랜\n");

        return prompt.toString();
    }
}