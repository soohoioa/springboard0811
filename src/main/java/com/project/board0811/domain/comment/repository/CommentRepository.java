package com.project.board0811.domain.comment.repository;

import com.project.board0811.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    /**
     * 루트 댓글(댓글, depth=0) 페이징 조회
     * - parent is null 조건으로 루트만
     * - createdAt 정렬은 Pageable(sorts)로 제어 권장
     */
    @Query("""
        select c
        from Comment c
        where c.board.id = :boardId
          and c.parent is null
        """)
    Page<Comment> findRootCommentsByBoardId(@Param("boardId") Long boardId, Pageable pageable);

    /**
     * 특정 parent에 달린 답글(대댓글) 목록
     * - 서비스에서 단건 조회용으로 쓸 수 있지만,
     *   일반적으로는 findRepliesByParentIds 로 일괄 조회 권장
     */
    List<Comment> findByParent_IdOrderByCreatedAtAsc(Long parentId);

    /**
     * 여러 루트 댓글들의 자식(답글)들을 한 번에 일괄 조회(IN)
     * - 트리 2단계(댓글/답글) 구조에서 N+1 방지의 핵심
     */
    @Query("""
        select c
        from Comment c
        where c.parent.id in :parentIds
        order by c.parent.id asc, c.createdAt asc
        """)
    List<Comment> findRepliesByParentIds(@Param("parentIds") Collection<Long> parentIds);

    /**
     * 게시글의 전체 댓글 수(루트+답글)
     * - 필요 시 카드/통계용
     */
    @Query("""
        select count(c)
        from Comment c
        where c.board.id = :boardId
        """)
    long countByBoardId(@Param("boardId") Long boardId);

}
