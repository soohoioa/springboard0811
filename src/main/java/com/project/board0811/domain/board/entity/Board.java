package com.project.board0811.domain.board.entity;

import com.project.board0811.domain.board.enums.BoardCategory;
import com.project.board0811.domain.board.enums.BoardStatus;
import com.project.board0811.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "board",
        indexes = {
                @Index(name = "idx_board_created_at", columnList = "created_at"),
                @Index(name = "idx_board_author_id", columnList = "author_id"),
                @Index(name = "idx_board_status", columnList = "status"),
                @Index(name = "idx_board_category", columnList = "category")
        }
)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_board_author"))
    private User author;

    @Column(nullable = false, length = 150)
    private String title;

    // 본문은 대용량 가능성 -> LOB
    @Lob
    @Column(nullable = false)
    private String content;

    // 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardCategory category = BoardCategory.FREE;

    // 게시글 상태 (공개/비공개/삭제)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardStatus status = BoardStatus.PUBLIC;

    // 조회수
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    // 낙관적 락
    @Version
    private Long version;

    // 소프트 삭제 시각 (DELETED 전환 시 기록)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Board(User author, String title, String content,
                  BoardCategory category, BoardStatus status) {
        this.author = author;
        this.title = title;
        this.content = content;
        if (category != null) this.category = category;
        if (status != null) this.status = status;
    }

    // 수정: 선택 필드만 변경
    public void update(String title, String content, BoardCategory category, BoardStatus status) {
        if (title != null && !title.isBlank()) this.title = title;
        if (content != null && !content.isBlank()) this.content = content;
        if (category != null) this.category = category;
        if (status != null) this.status = status;
        if (this.status != BoardStatus.DELETED) this.deletedAt = null; // 상태가 삭제가 아니면 삭제표시 해제
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    // 소프트 삭제
    public void softDelete() {
        this.status = BoardStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // 소프트 삭제 취소
    public void restore() {
        if (this.status == BoardStatus.DELETED) {
            this.status = BoardStatus.PRIVATE; // 기본값을 PRIVATE 로 복구하거나 필요에 따라 PUBLIC 로 변경 가능
            this.deletedAt = null;
        }
    }

    // 활성 글 여부 (삭제 아님)
    public boolean isActive() {
        return this.status != BoardStatus.DELETED;
    }
}
