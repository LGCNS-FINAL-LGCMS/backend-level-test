package com.lgcms.leveltest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "level_test_report")
@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_level", nullable = false, length = 10)
    private StudentLevel studentLevel;

    @Column(name = "comprehensive_feedback", columnDefinition = "TEXT")
    private String comprehensiveFeedback;

    @Column(name = "next_learning_recommendation", columnDefinition = "TEXT")
    private String nextLearningRecommendation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 개념 분석과의 관계
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReportConceptAnalysis> conceptAnalyses = new ArrayList<>();
}