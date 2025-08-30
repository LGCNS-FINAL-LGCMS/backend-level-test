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

    List<MemberAnswer> findTop10ByMemberIdAndIsScoredOrderByIdDesc(Long memberId, Boolean isScored);
}