package com.lgcms.leveltest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_answer",
        indexes = {
                @Index(name = "idx_member_answer_member_id", columnList = "member_id"),
                @Index(name = "idx_member_answer_question_id", columnList = "question_id"),
                @Index(name = "idx_member_answer_scored", columnList = "is_scored")
        })
@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LevelTest question;

    @Column(name = "member_answer", columnDefinition = "TEXT")
    private String memberAnswer;

    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Builder.Default
    @Column(name = "is_scored")
    private Boolean isScored = false;

    @Column(name = "scoring_details", columnDefinition = "TEXT")
    private String scoringDetails;

    @Column(name = "must_include_matched", columnDefinition = "TEXT")
    private String mustIncludeMatched;

    @Column(name = "scored_at")
    private LocalDateTime scoredAt;

    @Column(name = "scoring_model")
    private String scoringModel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}