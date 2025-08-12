package com.lgcms.leveltest.domain;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
public enum CategoryCriteria {

    ALGORITHM(
            List.of("음수 가중치", "충돌 없음", "항상 O(n log n)", "MST=최단경로", "DFS가 항상 최적", "해시는 정렬 구조"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("알고리즘 기본 개념과 동작 원리 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("알고리즘 분석과 복잡도 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("고급 알고리즘과 최적화 분석 능력 평가")
                            .build()
            )
    ),

    DATABASE(
            List.of("PRIMARY KEY는 NULL 허용", "트랜잭션=백업", "정규화는 무조건 성능 ↑", "인덱스는 모든 쿼리에 유리", "JOIN=카르테시안 곱"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("데이터베이스 기본 개념과 SQL 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("DB 설계와 정규화, 트랜잭션 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("고급 DB 최적화와 성능 튜닝 분석 능력 평가")
                            .build()
            )
    ),

    DOCKER(
            List.of("컨테이너=가상머신", "이미지 수정 시 컨테이너 자동 업데이트", "latest=항상 최신", "데이터=이미지에 저장"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Docker 기본 개념과 컨테이너 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Docker 이미지와 네트워킹, 볼륨 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("Docker Compose와 오케스트레이션 분석 능력 평가")
                            .build()
            )
    ),

    GIT(
            List.of("rebase=히스토리 보존", "reset은 항상 안전", "force push 안전", "merge=항상 fast-forward", "HEAD는 브랜치 이름"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Git 기본 명령어와 버전 관리 개념 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Git 브랜치 전략과 merge, rebase 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("Git 고급 기능과 협업 워크플로우 분석 능력 평가")
                            .build()
            )
    ),

    JAVA(
            List.of("String은 가변", "==와 equals 동일", "JVM=JDK", "Checked 예외=Runtime", "ArrayList는 thread-safe"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Java 기본 문법과 객체지향 개념 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Java 컬렉션과 예외처리, 메모리 관리 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("JVM 내부와 고급 Java 기능 분석 능력 평가")
                            .build()
            )
    ),

    NODEJS(
            List.of("단일 스레드=동시성 불가", "event loop=멀티스레드", "콜백=블로킹", "npm=런타임"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Node.js 기본 개념과 비동기 처리 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Event Loop와 모듈 시스템, NPM 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("Node.js 성능 최적화와 고급 패턴 분석 능력 평가")
                            .build()
            )
    ),

    PYTHON(
            List.of("list=링크드리스트", "GIL 때문에 병렬 전혀 불가", "is====", "mutable default 안전", "dict는 순서 없음"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Python 기본 문법과 데이터 타입 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Python 고급 기능과 메모리 관리, GIL 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("Python 내부 구조와 성능 최적화 분석 능력 평가")
                            .build()
            )
    ),

    REACT(
            List.of("state 직접 변경", "key에 index 사용 권장", "useEffect=mount만", "props는 변경 가능", "제어/비제어 혼용 OK"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("React 기본 개념과 컴포넌트 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("React Hooks와 상태 관리, 생명주기 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("React 성능 최적화와 고급 패턴 분석 능력 평가")
                            .build()
            )
    ),

    SPRING(
            List.of("싱글톤=전역변수", "트랜잭션=자동 저장", "Bean=Component 동일", "AOP=성능 최적화만", "DI=상속"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Spring 기본 개념과 DI/IoC 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Spring Bean 관리와 AOP, 트랜잭션 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("Spring Boot와 고급 설정, 아키텍처 분석 능력 평가")
                            .build()
            )
    ),

    VUEJS(
            List.of("v-if=v-show", "computed=methods", "key 생략", "양방향 바인딩 자동 마법", "watch만으로 상태 관리"),
            Map.of(
                    Difficulty.LOW, ScoringCriteria.builder()
                            .passThreshold(70)
                            .weights(Map.of("완전성", 0.4, "정확성", 0.4, "용어 적절성", 0.2))
                            .guideline("Vue.js 기본 개념과 디렉티브 이해도 평가")
                            .build(),
                    Difficulty.MEDIUM, ScoringCriteria.builder()
                            .passThreshold(75)
                            .weights(Map.of("완전성", 0.35, "정확성", 0.35, "개념적 깊이", 0.2, "용어 적절성", 0.1))
                            .guideline("Vue.js 컴포넌트와 Vuex, 라이프사이클 이해도 평가")
                            .build(),
                    Difficulty.HIGH, ScoringCriteria.builder()
                            .passThreshold(80)
                            .weights(Map.of("완전성", 0.3, "정확성", 0.3, "예시/근거", 0.15, "개념적 깊이", 0.25))
                            .guideline("Vue.js 고급 기능과 성능 최적화 분석 능력 평가")
                            .build()
            )
    );

    private final List<String> wrongKeywords;
    private final Map<Difficulty, ScoringCriteria> scoringCriteriaMap;

    CategoryCriteria(List<String> wrongKeywords, Map<Difficulty, ScoringCriteria> scoringCriteriaMap) {
        this.wrongKeywords = wrongKeywords;
        this.scoringCriteriaMap = scoringCriteriaMap;
    }

    public ScoringCriteria getScoringCriteria(Difficulty difficulty) {
        return scoringCriteriaMap.get(difficulty);
    }

    public int getPassThreshold(Difficulty difficulty) {
        return getScoringCriteria(difficulty).getPassThreshold();
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
        private final int passThreshold;
        private final Map<String, Double> weights;
        private final String guideline;
    }
}