package com.lgcms.leveltest.repository;

import com.lgcms.leveltest.domain.MemberAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberAnswerRepository extends JpaRepository<MemberAnswer, Long> {

    List<MemberAnswer> findByMemberId(Long memberId);

    Optional<MemberAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId);

    @Query("SELECT ua.question.id FROM MemberAnswer ua WHERE ua.memberId = :memberId")
    List<Long> findAnsweredQuestionIdsByMemberId(@Param("memberId") Long memberId);

    List<MemberAnswer> findByMemberIdAndIsScored(Long memberId, Boolean isScored);

    List<MemberAnswer> findByIsScored(Boolean isScored);

    @Query("SELECT COUNT(ma) FROM MemberAnswer ma WHERE ma.memberId = :memberId AND ma.isScored = true")
    long countScoredAnswersByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT AVG(ma.score) FROM MemberAnswer ma WHERE ma.memberId = :memberId AND ma.isScored = true")
    Double getAverageScoreByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT lt.category, AVG(ma.score) " +
            "FROM MemberAnswer ma " +
            "JOIN ma.question lt " +
            "WHERE ma.memberId = :memberId AND ma.isScored = true " +
            "GROUP BY lt.category")
    List<Object[]> getAverageScoreByCategory(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(CASE WHEN ma.isCorrect = true THEN 1 END) * 100.0 / COUNT(*) " +
            "FROM MemberAnswer ma " +
            "WHERE ma.memberId = :memberId AND ma.isScored = true")
    Double getCorrectRateByMemberId(@Param("memberId") Long memberId);
}