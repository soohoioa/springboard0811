package com.project.board0811.domain.board.controller;

import com.project.board0811.common.paging.PageRequestDto;
import com.project.board0811.common.paging.PageResponse;
import com.project.board0811.common.response.CommonApiResponse;
import com.project.board0811.domain.board.dto.request.BoardCreateRequestDto;
import com.project.board0811.domain.board.dto.request.BoardUpdateRequestDto;
import com.project.board0811.domain.board.dto.response.BoardResponseDto;
import com.project.board0811.domain.board.dto.response.BoardSummaryResponseDto;
import com.project.board0811.domain.board.enums.BoardCategory;
import com.project.board0811.domain.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final BoardService boardService;

    /** 게시글 등록 (로그인 사용자 = author) */
    @PostMapping
    public ResponseEntity<CommonApiResponse<BoardResponseDto>> create(
            @AuthenticationPrincipal(expression = "userId") Long authorId,
            @Valid @RequestBody BoardCreateRequestDto req
    ) {
        BoardResponseDto response = boardService.create(authorId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.ok(response));
    }

    /** 게시글 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<BoardResponseDto>> get(@PathVariable Long id) {
        BoardResponseDto response = boardService.get(id);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    /** 게시글 목록 (카테고리 선택 필터 + 공통 페이징) */
    @GetMapping
    public ResponseEntity<CommonApiResponse<PageResponse<BoardSummaryResponseDto>>> list(
            @Valid PageRequestDto pageReq,
            @RequestParam(required = false) BoardCategory category
    ) {
        PageResponse<BoardSummaryResponseDto> response = boardService.list(pageReq, category);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    /** 제목 검색 (삭제 제외) */
    @GetMapping("/search")
    public ResponseEntity<CommonApiResponse<PageResponse<BoardSummaryResponseDto>>> search(
            @RequestParam("q") String keyword,
            @Valid PageRequestDto pageReq
    ) {
        PageResponse<BoardSummaryResponseDto> response = boardService.searchByTitle(keyword, pageReq);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    /** 작성자 기준 목록 (삭제 제외) */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<CommonApiResponse<PageResponse<BoardSummaryResponseDto>>> listByAuthor(
            @PathVariable Long authorId,
            @Valid PageRequestDto pageReq
    ) {
        PageResponse<BoardSummaryResponseDto> response = boardService.listByAuthor(authorId, pageReq);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    /** 게시글 수정 (본인 또는 관리자) */
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<BoardResponseDto>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long editorUserId,
            @Valid @RequestBody BoardUpdateRequestDto req
    ) {
        BoardResponseDto response = boardService.update(id, editorUserId, req);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    /** 게시글 삭제(소프트) (본인 또는 관리자) */
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long requesterUserId
    ) {
        boardService.delete(id, requesterUserId);
        return ResponseEntity.noContent().build();
    }

    /** 조회수 증가 (단순 카운터 엔드포인트) */
    @PostMapping("/{id}/view")
    public ResponseEntity<CommonApiResponse<Void>> increaseView(@PathVariable Long id) {
        boardService.increaseViewCount(id);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }
}
