package com.project.board0811.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto { // 등록/수정 요청

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 2000, message = "댓글은 최대 2000자까지 가능합니다.")
    private String content;

    private Long parentId; // 답글인 경우 부모 댓글 id

    public CommentRequestDto(String content, Long parentId) {
        this.content = content;
        this.parentId = parentId;
    }
}
