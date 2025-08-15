package com.project.board0811.domain.board.service;

import com.project.board0811.common.paging.PageRequestDto;
import com.project.board0811.common.paging.PageResponse;
import com.project.board0811.domain.board.enums.BoardCategory;
import com.project.board0811.domain.board.dto.request.BoardCreateRequestDto;
import com.project.board0811.domain.board.dto.request.BoardUpdateRequestDto;
import com.project.board0811.domain.board.dto.response.BoardResponseDto;
import com.project.board0811.domain.board.dto.response.BoardSummaryResponseDto;

public interface BoardService {

    /** 게시글 생성 */
    BoardResponseDto create(Long authorId, BoardCreateRequestDto request);

    /** 단건 조회 (삭제글 제외) */
    BoardResponseDto get(Long id);

    /** 목록 조회 (카테고리 필터 선택) */
    PageResponse<BoardSummaryResponseDto> list(PageRequestDto pageReq, BoardCategory category);

    /** 제목 키워드 검색 (삭제글 제외) */
    PageResponse<BoardSummaryResponseDto> searchByTitle(String keyword, PageRequestDto pageReq);

    /** 작성자 기준 목록 (삭제글 제외) */
    PageResponse<BoardSummaryResponseDto> listByAuthor(Long authorId, PageRequestDto pageReq);

    /** 게시글 수정 (소유자 또는 관리자 권한 체크는 구현체에서 처리) */
    BoardResponseDto update(Long id, Long editorUserId, BoardUpdateRequestDto request);

    /** 소프트 삭제 */
    void delete(Long id, Long requesterUserId);

    /** 조회수 +1 (성공 시 true 반환하도록 해도 됨) */
    void increaseViewCount(Long id);
}
