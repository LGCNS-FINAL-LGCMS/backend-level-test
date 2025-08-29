package com.lgcms.leveltest.repository;

import com.lgcms.leveltest.domain.ReportConceptAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportConceptAnalysisRepository extends JpaRepository<ReportConceptAnalysis, Long> {
}