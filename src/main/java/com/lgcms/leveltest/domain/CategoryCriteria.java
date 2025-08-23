package com.lgcms.leveltest.domain;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
public enum CategoryCriteria {

    ALGORITHM(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("알고리즘 기본 개념과 동작 원리 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "복잡도 분석", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("알고리즘 분석과 복잡도 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "심화 분석", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("고급 알고리즘과 최적화 분석 능력 평가")
                            .build()
            )
    ),

    DATABASE(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("데이터베이스 기본 개념과 SQL 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "설계 능력", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("DB 설계와 정규화, 트랜잭션 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "최적화 분석", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("고급 DB 최적화와 성능 튜닝 분석 능력 평가")
                            .build()
            )
    ),

    DOCKER(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Docker 기본 개념과 컨테이너 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "아키텍처 이해", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Docker 이미지와 네트워킹, 볼륨 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "오케스트레이션 이해", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("Docker Compose와 오케스트레이션 분석 능력 평가")
                            .build()
            )
    ),

    GIT(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Git 기본 명령어와 버전 관리 개념 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "브랜치 전략 이해", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Git 브랜치 전략과 merge, rebase 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "협업 워크플로우", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("Git 고급 기능과 협업 워크플로우 분석 능력 평가")
                            .build()
            )
    ),

    JAVA(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Java 기본 문법과 객체지향 개념 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "메모리 관리 이해", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Java 컬렉션과 예외처리, 메모리 관리 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "JVM 내부 이해", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("JVM 내부와 고급 Java 기능 분석 능력 평가")
                            .build()
            )
    ),

    NODEJS(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Node.js 기본 개념과 비동기 처리 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "Event Loop 이해", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Event Loop와 모듈 시스템, NPM 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "성능 최적화", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("Node.js 성능 최적화와 고급 패턴 분석 능력 평가")
                            .build()
            )
    ),

    PYTHON(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Python 기본 문법과 데이터 타입 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "GIL과 메모리 이해", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Python 고급 기능과 메모리 관리, GIL 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "내부 구조 이해", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("Python 내부 구조와 성능 최적화 분석 능력 평가")
                            .build()
            )
    ),

    REACT(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("React 기본 개념과 컴포넌트 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "Hooks와 상태관리", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("React Hooks와 상태 관리, 생명주기 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "성능 최적화", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("React 성능 최적화와 고급 패턴 분석 능력 평가")
                            .build()
            )
    ),

    SPRING(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Spring 기본 개념과 DI/IoC 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "Bean과 AOP 이해", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Spring Bean 관리와 AOP, 트랜잭션 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "아키텍처 이해", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("Spring Boot와 고급 설정, 아키텍처 분석 능력 평가")
                            .build()
            )
    ),

    VUEJS(
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.5,
                                    "설명 완전성", 0.3,
                                    "용어 정확성", 0.2
                            ))
                            .guideline("Vue.js 기본 개념과 디렉티브 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "컴포넌트와 Vuex", 0.3,
                                    "설명 완전성", 0.2,
                                    "용어 정확성", 0.1
                            ))
                            .guideline("Vue.js 컴포넌트와 Vuex, 라이프사이클 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .weights(Map.of(
                                    "개념 이해도", 0.4,
                                    "성능 최적화", 0.3,
                                    "실무 적용성", 0.2,
                                    "논리적 전개", 0.1
                            ))
                            .guideline("Vue.js 고급 기능과 성능 최적화 분석 능력 평가")
                            .build()
            )
    );

    private final Map<Difficulty, ScoringCriteria> scoringCriteriaMap;

    CategoryCriteria(Map<Difficulty, ScoringCriteria> scoringCriteriaMap) {
        this.scoringCriteriaMap = scoringCriteriaMap;
    }

    public ScoringCriteria getScoringCriteria(Difficulty difficulty) {
        return scoringCriteriaMap.get(difficulty);
    }

    public String getGuideline(Difficulty difficulty) {
        return getScoringCriteria(difficulty).getGuideline();
    }

    public Map<String, Double> getWeights(Difficulty difficulty) {
        return getScoringCriteria(difficulty).getWeights();
    }

    // 내부 클래스
    @Getter
    @Builder
    public static class ScoringCriteria {
        private final Map<String, Double> weights;
        private final String guideline;
    }
}