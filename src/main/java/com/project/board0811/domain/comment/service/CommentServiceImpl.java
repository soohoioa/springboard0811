package com.project.board0811.domain.comment.service;

import com.project.board0811.common.exception.CustomException;
import com.project.board0811.common.exception.ErrorCode;
import com.project.board0811.domain.board.entity.Board;
import com.project.board0811.domain.board.repository.BoardRepository;
import com.project.board0811.domain.comment.dto.CommentRequestDto;
import com.project.board0811.domain.comment.dto.CommentResponseDto;
import com.project.board0811.domain.comment.dto.CommentWithRepliesDto;
import com.project.board0811.domain.comment.entity.Comment;
import com.project.board0811.domain.comment.repository.CommentRepository;
import com.project.board0811.domain.user.entity.User;
import com.project.board0811.domain.user.enums.UserRole;
import com.project.board0811.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * 댓글/답글 생성
     */
    @Override
    public CommentResponseDto create(Long boardId, Long authorUserId, CommentRequestDto request) {
        // 필수 엔티티 조회: 존재하지 않으면 404 성격의 CustomException
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        User author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 댓글/답글 분기 (parentId null → 루트 댓글, not null → 답글)
        Comment saved;
        if (request.getParentId() == null) {
            // 루트 댓글 생성: 유효성(내용/길이) 검증은 도메인 팩토리에서 수행
            saved = commentRepository.save(Comment.newRoot(board, author, request.getContent()));
        } else {
            // 답글 생성: 부모 댓글 존재 검증 후 팩토리 호출 (루트/보드 일치 검증 포함)
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            saved = commentRepository.save(Comment.newReply(board, author, request.getContent(), parent));
        }

        // 표현 책임은 DTO로 위임 (삭제 마스킹 등)
        return CommentResponseDto.fromEntity(saved);
    }

    /**
     * 루트 댓글 페이지 + 각 루트의 답글(대댓글) 묶음 조회
     */
    @Transactional(readOnly = true) // 조회 전용 트랜잭션: 영속성 컨텍스트가 dirty-check를 하지 않아 성능상 유리
    @Override
    public Page<CommentWithRepliesDto> getCommentTreePage(Long boardId, Pageable pageable) {
        // 1) 루트 댓글 페이지 조회
        Page<Comment> rootPage = commentRepository.findRootPage(boardId, pageable);

        // 2) 루트 댓글 id 목록 추출
        List<Long> rootIds = rootPage.getContent().stream()
                .map(Comment::getId)
                .toList();

        // 3) 루트들의 자식(답글) 일괄 조회(IN) → parentId -> List<Comment>
        Map<Long, List<Comment>> replyMap = rootIds.isEmpty()
                ? Collections.emptyMap()
                : commentRepository.findRepliesGroupedByParentIds(rootIds);

        // 4) 트리 DTO 조립 (삭제 마스킹/작성자 노출 등은 DTO에서 처리)
        List<CommentWithRepliesDto> content = rootPage.getContent().stream()
                .map(root -> CommentWithRepliesDto.of(root, replyMap.getOrDefault(root.getId(), List.of())))
                .collect(Collectors.toList());

        // 5) 기존 페이지 메타데이터(total 등)는 유지한 채 DTO 페이지 생성
        return new PageImpl<>(content, pageable, rootPage.getTotalElements());
    }

    /**
     * 댓글 내용 수정
     */
    @Override
    public CommentResponseDto update(Long commentId, Long requesterUserId, CommentRequestDto request) {
        // 대상 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 요청자 조회
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 체크 (작성자/관리자만 허용)
        checkOwnershipOrAdmin(comment, requester);

        // 내용 변경: 도메인 유효성(삭제/공백/길이) 내부 검증
        comment.changeContent(request.getContent());

        // JPA Dirty Checking 으로 트랜잭션 커밋 시 자동 반영
        return CommentResponseDto.fromEntity(comment);
    }

    /**
     * 댓글 소프트 삭제
     */
    @Override
    public void delete(Long commentId, Long requesterUserId) {
        // 대상 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 요청자 조회
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 체크
        checkOwnershipOrAdmin(comment, requester);

        // 소프트 삭제 플래그 변경
        comment.softDelete();
        // 필요 시 삭제 이력/감사 로그/이벤트 발행 등 확장 가능
    }

    /**
     * 좋아요 증가
     * - 단순 카운터 증가. 높은 동시성에서 정확성을 보장하려면 낙관적 락(@Version) 또는 DB 레벨 증분 쿼리 고려
     */
    @Override
    public void like(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.increaseLike();
    }

    /**
     * 좋아요 감소
     * - 0 미만으로 내려가지 않도록 도메인에서 가드
     */
    @Override
    public void unlike(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.decreaseLike();
    }

    /**
     * 권한 체크 유틸
     * - 작성자 본인 또는 관리자(ROLE_ADMIN)만 허용
     * - 실패 시 FORBIDDEN 예외
     */
    private void checkOwnershipOrAdmin(Comment comment, User requester) {
        boolean isOwner = comment.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ROLE_ADMIN;

        if (!(isOwner || isAdmin)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
