package com.project.board0811.domain.comment.entity;

import com.project.board0811.common.exception.CustomException;
import com.project.board0811.common.exception.ErrorCode;
import com.project.board0811.common.model.BaseTimeEntity;
import com.project.board0811.domain.board.entity.Board;
import com.project.board0811.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comment_post_parent_created", columnList = "board_id, parent_id, created_at"),
                @Index(name = "idx_comment_post_depth_created", columnList = "board_id, depth, created_at"),
                @Index(name = "idx_comment_user_created", columnList = "user_id, created_at")
        }
)
@Check(constraints = "depth in (0,1)") // Hibernate 전용 CHECK
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연관 관계
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // null이면 댓글, 있으면 답글

    /**
     * 속성
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 0 = 댓글, 1 = 답글
     */
    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private int likeCount = 0;

    @Builder(access = AccessLevel.PRIVATE) // 외부에서 직접 Builder 사용하지 않도록
    private Comment(Board board, User author, Comment parent, String content, int depth) {
        this.board = board;
        this.author = author;
        this.parent = parent;
        this.content = content;
        this.depth = depth;
    }

    /**
     * 루트 댓글 생성 (depth=0)
     */
    public static Comment newRoot(Board board, User author, String content) {
        validateRequired(board, author, content);
        return Comment.builder()
                .board(board)
                .author(author)
                .content(content)
                .depth(0)
                .build();
    }

    /**
     * 답글 생성 (depth=1) — 반드시 부모는 루트 댓글이어야 함.
     */
    public static Comment newReply(Board board, User author, String content, Comment parent) {
        validateRequired(board, author, content);
        if (parent == null) {
            throw new CustomException(ErrorCode.COMMENT_PARENT_REQUIRED);
        }
        if (parent.depth != 0) {
            throw new CustomException(ErrorCode.COMMENT_INVALID_DEPTH);
        }
        if (!parent.getBoard().getId().equals(board.getId())) {
            throw new CustomException(ErrorCode.COMMENT_BOARD_MISMATCH);
        }
        return Comment.builder()
                .board(board)
                .author(author)
                .parent(parent)
                .content(content)
                .depth(1)
                .build();
    }

    private static void validateRequired(Board board, User author, String content) {
        if (board == null) throw new CustomException(ErrorCode.COMMENT_BOARD_REQUIRED);
        if (author == null) throw new CustomException(ErrorCode.COMMENT_AUTHOR_REQUIRED);
        if (content == null || content.isBlank()) {
            throw new CustomException(ErrorCode.COMMENT_CONTENT_EMPTY);
        }
        if (content.length() > 2000) {
            throw new CustomException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
        }
    }

    // === 도메인 동작 ===
    public void changeContent(String newContent) {
        if (isDeleted) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
        }
        if (newContent == null || newContent.isBlank()) {
            throw new CustomException(ErrorCode.COMMENT_CONTENT_EMPTY);
        }
        if (newContent.length() > 2000) {
            throw new CustomException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
        }
        this.content = newContent;
    }

    public void softDelete() {
        this.isDeleted = true;
        // 실제 노출은 DTO 레벨에서 "[삭제된 댓글입니다]"로 마스킹 권장
    }

    public void increaseLike() { this.likeCount++; }

    public void decreaseLike() {
        if (this.likeCount > 0) this.likeCount--;
    }

    /**
     * 편의 메서드
     */
    public boolean isRoot() { return depth == 0; }

    public boolean isReply() { return depth == 1; }

    // equals/hashCode는 식별자 기반 권장
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
