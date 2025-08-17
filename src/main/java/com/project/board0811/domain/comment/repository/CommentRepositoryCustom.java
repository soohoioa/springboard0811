package com.project.board0811.domain.comment.repository;

import com.project.board0811.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommentRepositoryCustom {
    /**
     * 루트 댓글 페이지 조회 (정렬 포함)
     * - QueryDSL/JPQL로 필요시 조건 확장 (isDeleted 필터 등)
     */
    Page<Comment> findRootPage(Long boardId, Pageable pageable);

    /**
     * parentIds(루트 댓글 id들)에 대한 자식(답글) 일괄 조회 후
     * parentId -> List<Comment> 맵핑
     */
    Map<Long, List<Comment>> findRepliesGroupedByParentIds(Collection<Long> parentIds);
}
