package com.project.board0811.domain.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.board0811.domain.board.entity.Board;
import com.project.board0811.domain.board.enums.BoardCategory;
import com.project.board0811.domain.board.enums.BoardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardSummaryResponseDto { // 목록(요약) 조회

    private Long id;
    private String title;
    private BoardCategory category;
    private BoardStatus status;
    private int viewCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String authorName;

    public static BoardSummaryResponseDto from(Board board) {
        return BoardSummaryResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .category(board.getCategory())
                .status(board.getStatus())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .authorName(board.getAuthor().getName())
                .build();
    }

}
