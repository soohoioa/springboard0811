package com.project.board0811.dto.request;

import com.project.board0811.domain.Board;
import com.project.board0811.domain.enums.BoardCategory;
import com.project.board0811.domain.enums.BoardStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardUpdateRequestDto {

    @Size(max = 50)
    private String title; // null이면 변경 안함

    private String content; // null이면 변경 안함

    private BoardCategory category; // null이면 변경 안함

    private BoardStatus status; // null이면 변경 안함

    // 부분 업데이트 적용
    public void applyTo(Board board) {
        board.update(title, content, category, status);
    }
}
