package com.project.board0811.domain.comment.dto;

import com.project.board0811.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto { // 단건 댓글 응답

    private Long id;
    private String content;
    private Long parentId;
    private int depth;
    private boolean deleted;
    private int likeCount;

    private Long authorId;
    private String authorName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.isDeleted() ? "[삭제된 댓글입니다]" : comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .depth(comment.getDepth())
                .deleted(comment.isDeleted())
                .likeCount(comment.getLikeCount())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getUsername()) // User 엔티티에 username 필드 있다고 가정
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
