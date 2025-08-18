package com.project.board0811.domain.comment.service;

import com.project.board0811.domain.comment.dto.CommentRequestDto;
import com.project.board0811.domain.comment.dto.CommentResponseDto;
import com.project.board0811.domain.comment.dto.CommentWithRepliesDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    /** 댓글/답글 생성 (parentId null이면 루트 댓글, 아니면 답글) */
    CommentResponseDto create(Long boardId, Long authorUserId, CommentRequestDto request);

    /** 게시글의 루트 댓글 페이지 + 각 루트의 답글 일괄 묶음 */
    Page<CommentWithRepliesDto> getCommentTreePage(Long boardId, Pageable pageable);

    /** 댓글 내용 수정 (작성자 or 관리자 권한) */
    CommentResponseDto update(Long commentId, Long requesterUserId, CommentRequestDto request);

    /** 소프트 삭제 (작성자 or 관리자 권한) */
    void delete(Long commentId, Long requesterUserId);

    /** 좋아요 증가 */
    void like(Long commentId);

    /** 좋아요 감소 (0 미만 방지) */
    void unlike(Long commentId);

}
