package com.project.board0811.service;

import com.project.board0811.common.exception.CustomException;
import com.project.board0811.common.exception.ErrorCode;
import com.project.board0811.common.paging.PageConverters;
import com.project.board0811.common.paging.PageRequestDto;
import com.project.board0811.common.paging.PageResponse;
import com.project.board0811.domain.Board;
import com.project.board0811.domain.User;
import com.project.board0811.domain.enums.BoardCategory;
import com.project.board0811.domain.enums.BoardStatus;
import com.project.board0811.domain.enums.UserRole;
import com.project.board0811.dto.request.BoardCreateRequestDto;
import com.project.board0811.dto.request.BoardUpdateRequestDto;
import com.project.board0811.dto.response.BoardResponseDto;
import com.project.board0811.dto.response.BoardSummaryResponseDto;
import com.project.board0811.repository.BoardRepository;
import com.project.board0811.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // ====== Create ======
    @Override
    public BoardResponseDto create(Long authorId, BoardCreateRequestDto request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board board = request.toEntity(author);
        Board saved = boardRepository.save(board);
        return BoardResponseDto.from(saved);
    }

    // ====== Read One ======
    @Transactional(readOnly = true)
    @Override
    public BoardResponseDto get(Long id) {
        Board board = findActiveBoardOrThrow(id);
        return BoardResponseDto.from(board);
    }

    // ====== List ======
    @Transactional(readOnly = true)
    @Override
    public PageResponse<BoardSummaryResponseDto> list(PageRequestDto pageReq, BoardCategory category) {
        var pageable = pageReq.toPageable();
        Page<Board> page = (category == null)
                ? boardRepository.findAllByStatusNot(BoardStatus.DELETED, pageable)
                : boardRepository.findByStatusAndCategory(BoardStatus.PUBLIC, category, pageable);

        return PageConverters.toResponse(page, BoardSummaryResponseDto::from,
                pageReq.getSort(), pageReq.getDirection());
    }

    // ====== Search by Title ======
    @Transactional(readOnly = true)
    @Override
    public PageResponse<BoardSummaryResponseDto> searchByTitle(String keyword, PageRequestDto pageReq) {
        var pageable = pageReq.toPageable();
        Page<Board> page = boardRepository.findByTitleContainingIgnoreCaseAndStatusNot(
                keyword, BoardStatus.DELETED, pageable);

        return PageConverters.toResponse(page, BoardSummaryResponseDto::from,
                pageReq.getSort(), pageReq.getDirection());
    }

    // ====== List by Author ======
    @Transactional(readOnly = true)
    @Override
    public PageResponse<BoardSummaryResponseDto> listByAuthor(Long authorId, PageRequestDto pageReq) {
        var pageable = pageReq.toPageable();
        Page<Board> page = boardRepository.findByAuthor_IdAndStatusNot(
                authorId, BoardStatus.DELETED, pageable);

        return PageConverters.toResponse(page, BoardSummaryResponseDto::from,
                pageReq.getSort(), pageReq.getDirection());
    }

    // ====== Update ======
    @Override
    public BoardResponseDto update(Long id, Long editorUserId, BoardUpdateRequestDto request) {
        Board board = findActiveBoardOrThrow(id);

        User editor = userRepository.findById(editorUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        checkOwnershipOrAdmin(board, editor); // 권한 체크 → FORBIDDEN

        request.applyTo(board); // 엔티티 update 호출
        return BoardResponseDto.from(board);
    }

    // ====== Soft Delete ======
    @Override
    public void delete(Long id, Long requesterUserId) {
        Board board = boardRepository.findWithAuthorById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        checkOwnershipOrAdmin(board, requester);

        // 상태 전환 + deletedAt 기록 (쿼리)
        if (boardRepository.softDeleteById(id) == 0) {
            // 이미 삭제되었거나 대상 아님 → 논리적으로는 Not Found 취급
            throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
        }
    }

    // ====== Increase View Count ======
    @Override
    public void increaseViewCount(Long id) {
        int updated = boardRepository.increaseViewCount(id);
        if (updated == 0) {
            throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
        }
    }

    // ====== Helpers ======
    @Transactional(readOnly = true)
    protected Board findActiveBoardOrThrow(Long id) {
        return boardRepository.findWithAuthorById(id)
                .filter(b -> b.getStatus() != BoardStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    protected void checkOwnershipOrAdmin(Board board, User actor) {
        boolean isOwner = board.getAuthor().getId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == UserRole.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}