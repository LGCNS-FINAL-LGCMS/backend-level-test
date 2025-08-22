package com.lgcms.leveltest.service.grading;

import com.lgcms.leveltest.domain.CategoryCriteria;
import com.lgcms.leveltest.domain.LevelTest;
import com.lgcms.leveltest.domain.MemberAnswer;

import java.util.List;
import java.util.stream.Collectors;

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

    private static final String GRADING_TEMPLATE = """
        다음 문제에 대한 학생의 답변을 채점해주세요.
        
        [문제 정보]
        카테고리: %s
        난이도: %s
        문제: %s
        
        [모범 답안]
        %s
        
        [채점 기준]
        %s
        
        [학생 답변]
        %s
        
        [채점 요청]
        %s
        """;

    private static final String COMPREHENSIVE_FEEDBACK_TEMPLATE = """
        다음은 학생의 프로그래밍 레벨테스트 전체 답변입니다. 종합적인 피드백을 제공해주세요.
        
        [전체 성과] 평균 점수: %.1f점 (총 %d문제)
        
        %s
        
        다음 형식으로 종합 피드백을 제공해주세요:
        
        **전체 실력 분석**
        - 평균 점수를 바탕으로 한 현재 실력 수준 평가
        - 전반적인 개념 이해도와 학습 상태
        
        **강점 영역**
        - 잘 이해하고 있는 개념들과 높은 점수를 받은 영역
        - 앞으로 더 발전시킬 수 있는 부분
        
        **개선이 필요한 영역**
        - 낮은 점수를 받은 영역과 부족한 개념들
        - 우선적으로 학습해야 할 주제들
        
        **맞춤 학습 가이드**
        - 현재 수준에 맞는 구체적인 학습 방향
        - 단계별 학습 추천 사항
        
        **실력 향상을 위한 조언**
        - 효과적인 학습 방법과 실습 방향
        - 다음 레벨로 올라가기 위한 구체적인 액션 플랜
        """;

    public static String buildPrompt(LevelTest question, String memberAnswer) {
        return String.format(GRADING_TEMPLATE,
                question.getCategory().getCategoryName(),
                question.getDifficulty().getDifficultyName(),
                question.getQuestion(),
                question.getAnswer(),
                buildScoringCriteria(question),
                memberAnswer != null ? memberAnswer : "(답변 없음)",
                getGradingInstructions()
        );
    }

    public static String buildComprehensiveFeedbackPrompt(List<MemberAnswer> answers) {
        double avgScore = answers.stream()
                .mapToInt(a -> a.getScore() != null ? a.getScore() : 0)
                .average()
                .orElse(0.0);

        String questionsAndAnswers = buildQuestionsAndAnswersSection(answers);

        return String.format(COMPREHENSIVE_FEEDBACK_TEMPLATE,
                avgScore,
                answers.size(),
                questionsAndAnswers
        );
    }

    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    private static String buildScoringCriteria(LevelTest question) {
        CategoryCriteria criteria = CategoryCriteria.valueOf(question.getCategory().name());
        CategoryCriteria.ScoringCriteria scoringCriteria = criteria.getScoringCriteria(question.getDifficulty());

        StringBuilder criteriaBuilder = new StringBuilder();

        // 핵심 키워드 섹션
        if (question.getMustInclude() != null && !question.getMustInclude().isEmpty()) {
            criteriaBuilder.append("핵심 키워드 (포함 권장):\n");
            String keywords = question.getMustInclude().stream()
                    .map(keyword -> "  - " + keyword)
                    .collect(Collectors.joining("\n"));
            criteriaBuilder.append(keywords).append("\n");
            criteriaBuilder.append("※ 키워드가 없어도 개념을 올바르게 설명했다면 적절한 점수 부여\n\n");
        }

        // 평가 가중치 섹션
        criteriaBuilder.append("평가 가중치:\n");
        String weights = scoringCriteria.getWeights().entrySet().stream()
                .map(entry -> String.format("  - %s: %d%%", entry.getKey(), (int)(entry.getValue() * 100)))
                .collect(Collectors.joining("\n"));
        criteriaBuilder.append(weights).append("\n\n");

        // 평가 방향
        criteriaBuilder.append("평가 방향: ").append(scoringCriteria.getGuideline());

        return criteriaBuilder.toString();
    }

    private static String buildQuestionsAndAnswersSection(List<MemberAnswer> answers) {
        return answers.stream()
                .map(answer -> String.format(
                        "[문제 %d] (%s - %s) %s\n[학생답변] %s\n[점수] %d점",
                        answers.indexOf(answer) + 1,
                        answer.getQuestion().getCategory().getCategoryName(),
                        answer.getQuestion().getDifficulty().getDifficultyName(),
                        answer.getQuestion().getQuestion(),
                        answer.getMemberAnswer(),
                        answer.getScore() != null ? answer.getScore() : 0
                ))
                .collect(Collectors.joining("\n\n"));
    }

    private static String getGradingInstructions() {
        return """
        위 정보를 바탕으로 다음 형식의 JSON으로 채점 결과를 제공해주세요:
        
        {
            "score": (0-100 사이의 정수),
            "feedback": "전체적인 피드백 (학생의 이해도 수준과 개선 방향)",
            "strengths": "잘한 점 또는 올바르게 이해한 부분 (없으면 '기본 개념 학습 필요')",
            "improvements": "개선할 점 및 추가 학습이 필요한 영역",
            "mustIncludeMatched": ["언급된", "핵심", "키워드"],
            "conceptAnalyses": [
                {
                    "conceptName": "개념명",
                    "score": (1-5 사이의 정수),
                    "comment": "해당 개념에 대한 간단한 평가"
                }
            ],
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
        
        **개념 분석 가이드라인:**
        8. 이 문제에서 다루는 주요 개념들을 1-3개 정도 식별
        9. 각 개념에 대한 학생의 이해도를 1-5점으로 평가 (별점 표시용)
        10. 개념별 코멘트는 10-15자 이내로 간결하게 작성
        11. 개념명은 구체적이고 명확하게 작성 (예: "DI/IoC", "Bean 생명주기", "조건문과 반복문" 등)
        
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
}