package com.lgcms.leveltest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_answer",
        indexes = {
                @Index(name = "idx_member_answer_member_id", columnList = "member_id"),
                @Index(name = "idx_member_answer_question_id", columnList = "question_id"),
                @Index(name = "idx_member_answer_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}