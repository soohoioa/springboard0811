package com.project.board0811.domain.board.repository;

import com.project.board0811.domain.board.entity.Board;
import com.project.board0811.domain.board.enums.BoardCategory;
import com.project.board0811.domain.board.enums.BoardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // ===== 단건 조회 =====
    Optional<Board> findByIdAndStatusNot(Long id, BoardStatus status); // 삭제(DELETED) 아닌 글 단건

    @EntityGraph(attributePaths = "author")
    @Query("select b from Board b where b.id = :id")
    Optional<Board> findWithAuthorById(@Param("id") Long id); // 작성자(author)까지 즉시 로딩하여 단건 조회


    // ===== 목록 조회 =====
    // 상태/카테고리 별 페이지 조회
    Page<Board> findByStatusAndCategory(BoardStatus status, BoardCategory category, Pageable pageable);

    // 삭제 아닌 전체 목록
    Page<Board> findAllByStatusNot(BoardStatus status, Pageable pageable);

    // 작성자 기준 목록 (삭제 제외)
    Page<Board> findByAuthor_IdAndStatusNot(Long authorId, BoardStatus status, Pageable pageable);

    // 제목 키워드 검색 (삭제 제외)
    Page<Board> findByTitleContainingIgnoreCaseAndStatusNot(String title, BoardStatus status, Pageable pageable);


    // ===== 부가 기능 =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Board b set b.viewCount = b.viewCount + 1 where b.id = :id")
    int increaseViewCount(@Param("id") Long id); // 본문/제목 없이 조회수만 안전하게 증가

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Board b
              set b.status = com.project.board0811.domain.enums.BoardStatus.DELETED,
                  b.deletedAt = current_timestamp
            where b.id = :id
              and b.status <> com.project.board0811.domain.enums.BoardStatus.DELETED
           """)
    int softDeleteById(@Param("id") Long id); // 소프트 삭제(상태 전환 + deletedAt 기록)

    boolean existsByIdAndAuthor_Id(Long id, Long authorId); // 작성자 소유 여부 체크 (권한 확인에 사용)
}