package com.project.board0811.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.board0811.domain.Board;
import com.project.board0811.domain.enums.BoardCategory;
import com.project.board0811.domain.enums.BoardStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponseDto { // 단건 조회

    private Long id;

    private String title;

    private String content;

    private BoardCategory category;

    private BoardStatus status;

    private int viewCount;

    private Long version;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;


    private AuthorSummary author; // 작성자 요약 정보

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorSummary {
        private Long id;
        private String username;
        private String name;
        private String email;
    }

    public static BoardResponseDto from(Board board) {
        return BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getCategory())
                .status(board.getStatus())
                .viewCount(board.getViewCount())
                .version(board.getVersion())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .deletedAt(board.getDeletedAt())
                .author(AuthorSummary.builder()
                        .id(board.getAuthor().getId())
                        .username(board.getAuthor().getUsername())
                        .name(board.getAuthor().getName())
                        .email(board.getAuthor().getEmail())
                        .build())
                .build();
    }

}
