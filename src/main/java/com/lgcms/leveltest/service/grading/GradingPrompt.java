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
        다음은 학생의 프로그래밍 레벨테스트 전체 답변입니다.
        
        [전체 성과] 평균 점수: %.1f점 (총 %d문제)
        
        %s
        
        위 결과를 바탕으로 학생의 실력을 종합 평가하여 2-4줄의 간단한 피드백을 작성해주세요.
        다음 내용을 자연스럽게 포함하되 소제목 없이 하나의 문단으로 작성하세요:
        
        - 전반적인 실력 수준과 이해도
        - 주요 강점 영역 1-2개
        - 가장 시급한 개선점 1-2개
        - 구체적인 학습 방향 제시
        
        ※ 마크다운 형식이나 소제목 없이 일반 텍스트로만 작성하세요.
        ※ 2-4문장으로 간결하게 작성하세요.
        ※ 이 학생은' 같은 3인칭 표현은 사용하지 말고, 주어를 생략하거나 직접적인 어조로 작성하세요.
        """;

    public static String buildPrompt(LevelTest question, String memberAnswer) {
        return String.format(GRADING_TEMPLATE,
                question.getCategory().getCategoryName(),
                question.getDifficulty().getDifficultyName(),
                question.getQuestion(),
                question.getAnswer(),
                buildScoringCriteria(question),
                memberAnswer == null || memberAnswer.isBlank() ? "(답변 없음)" : memberAnswer,
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
                .map(answer -> {
                    String memberAnswerText = (answer.getMemberAnswer() == null || answer.getMemberAnswer().isBlank())
                            ? "(답변 없음)"
                            : answer.getMemberAnswer();

                    return String.format(
                            "[문제 %d] (%s - %s) %s\n[학생답변] %s\n[점수] %d점",
                            answers.indexOf(answer) + 1,
                            answer.getQuestion().getCategory().getCategoryName(),
                            answer.getQuestion().getDifficulty().getDifficultyName(),
                            answer.getQuestion().getQuestion(),
                            memberAnswerText,
                            answer.getScore() != null ? answer.getScore() : 0
                    );
                })
                .collect(Collectors.joining("\n\n"));
    }

    private static String getGradingInstructions() {
        return """
           위 정보를 바탕으로 다음 형식의 JSON으로 채점 결과를 제공해주세요:
           
           {
               "score": (문제 난이도별 만점 기준의 정수 - 하급:8점, 중급:10점, 상급:12점),
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
           
           **중요: 점수는 다음 기준으로 채점하세요:**
           - LOW(하) 난이도: 0-8점 만점
           - MEDIUM(중) 난이도: 0-10점 만점
           - HIGH(상) 난이도: 0-12점 만점
           
           **scoringDetails 작성 가이드라인:**
           1. **[채점 기준]에 제시된 '평가 가중치' 항목들을 `criterion`으로 사용하세요.**
              - 예시: 평가 가중치에 "개념 이해도: 50%", "설명 완전성: 30%"가 있다면, `scoringDetails` 배열에는 `criterion`이 "개념 이해도"인 객체와 "설명 완전성"인 객체가 각각 포함되어야 합니다.
           2. 각 `criterion`의 `points`(배점)는 문제의 총점에 해당 가중치를 곱하여 정수로 계산하세요. (예: 10점 만점 문제에 가중치 30% -> 3점)
           
           채점 가이드라인:
           1. 개념 이해도를 가장 중요하게 평가 (핵심 내용의 정확성)
           2. 핵심 키워드 포함 여부 확인하되, 키워드가 없어도 올바른 설명이면 인정
           3. 부분적 이해도 적절히 점수로 반영 (0점, 만점 극단 지양)
           4. 답변이 없거나 완전히 틀린 경우에만 낮은 점수 부여
           5. 건설적이고 격려하는 톤으로 피드백 작성
           6. strengths는 구체적 내용 작성 필수 (빈 문자열 금지)
           7. 합격/불합격 판정하지 않고 점수로만 평가
           
           **개념 분석 가이드라인:**
           8. 이 문제에서 다루는 주요 개념들을 1-3개 정도 식별
           9. **중요: 각 개념에 대한 학생의 이해도를 1-5점으로 평가 (별점 표시용)**
           10. 개념별 코멘트는 10-15자 이내로 간결하게 작성
           11. 개념명은 구체적이고 명확하게 작성 (예: "DI/IoC", "Bean 생명주기" 등)
           12. **중요: 의미가 중복되는 개념들은 하나로 통합하여 작성**
               - 예시: "SpEL 기능"과 "SpEL 주요 용도" → "SpEL(Spring Expression Language)"로 통합
               - 예시: "Spring Data Commons", "Spring Data Commons 버전 관리" → "Spring Data Commons"로 통합
           
           점수 기준:
           - 만점의 90-100%: 매우 정확하고 완전한 이해
           - 만점의 80-89%: 핵심 개념 이해, 일부 세부사항 부족
           - 만점의 70-79%: 기본 개념 이해, 설명 미흡하거나 일부 오류
           - 만점의 60-69%: 부분적 이해, 중요한 개념 누락 또는 오해
           - 만점의 50-59%: 최소한의 이해, 많은 부분 부정확
           - 만점의 40% 이하: 답변 없음 또는 완전히 잘못된 이해
           
           **반드시 유효한 JSON 형식으로만 응답하세요.**
           """;
    }
}