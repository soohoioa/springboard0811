package com.project.board0811.domain;

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
                @Index(name = "idx_board_author_id", columnList = "author_id")
        }
)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 추후 User 엔티티로 교체 예정 (ManyToOne) */
    @Column(name = "author_id", nullable = false, updatable = false)
    private Long authorId;

    @Column(nullable = false, length = 150)
    private String title;

    /** 본문은 대용량 가능성 -> LOB */
    @Lob
    @Column(nullable = false)
    private String content;

    /** 공개/비공개 */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    /** 낙관적 락 */
    @Version
    private Long version;

    /** 소프트 삭제 시각 (null이면 활성) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Board(Long authorId, String title, String content, Boolean isPublic) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        if (isPublic != null) this.isPublic = isPublic;
    }

    /** 선택 필드만 변경 */
    public void update(String title, String content, Boolean isPublic) {
        if (title != null && !title.isBlank()) this.title = title;
        if (content != null && !content.isBlank()) this.content = content;
        if (isPublic != null) this.isPublic = isPublic;
    }

    /** 조회수 증가 */
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    /** 소프트 삭제 */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /** 소프트 삭제 취소 (필요 시) */
    public void restore() {
        this.deletedAt = null;
    }

    /** 활성 글 여부 */
    public boolean isActive() {
        return this.deletedAt == null;
    }
}
