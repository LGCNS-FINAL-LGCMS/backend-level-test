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

    // 사용자의 모든 답변 조회
    List<MemberAnswer> findByMemberId(Long memberId);

    // 사용자의 특정 문제 답변 조회 (중복 체크용)
    Optional<MemberAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId);

    // 사용자가 이미 답변한 문제 ID 목록
    @Query("SELECT ua.question.id FROM MemberAnswer ua WHERE ua.memberId = :memberId")
    List<Long> findAnsweredQuestionIdsByMemberId(@Param("memberId") Long memberId);
}