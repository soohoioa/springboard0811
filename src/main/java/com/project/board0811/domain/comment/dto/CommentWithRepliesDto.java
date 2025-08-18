package com.project.board0811.domain.comment.dto;

import com.project.board0811.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CommentWithRepliesDto { // 댓글 + 답글 묶음 응답

    private CommentResponseDto root; // 부모 댓글
    private List<CommentResponseDto> replies; // 자식 답글들

    public static CommentWithRepliesDto of(Comment root, List<Comment> replies) {
        return CommentWithRepliesDto.builder()
                .root(CommentResponseDto.fromEntity(root))
                .replies(replies.stream()
                        .map(CommentResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
