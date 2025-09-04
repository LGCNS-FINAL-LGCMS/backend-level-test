package com.lgcms.leveltest.repository;

import com.lgcms.leveltest.domain.LevelTestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelTestReportRepository extends JpaRepository<LevelTestReport, Long> {

    // 특정 회원의 레포트 목록 조회 (최신 3개)
    List<LevelTestReport> findTop3ByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 개념별 이해도 요약 조회
    @Query("SELECT r FROM LevelTestReport r LEFT JOIN FETCH r.conceptAnalyses WHERE r.id = :reportId")
    Optional<LevelTestReport> findByIdWithConceptAnalyses(@Param("reportId") Long reportId);
}
