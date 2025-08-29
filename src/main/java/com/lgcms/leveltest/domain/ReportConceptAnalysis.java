package com.lgcms.leveltest.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_concept_analysis")
@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportConceptAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private LevelTestReport report;

    @Column(name = "concept_name", nullable = false, length = 100)
    private String conceptName;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}