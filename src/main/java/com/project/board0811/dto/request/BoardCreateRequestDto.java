package com.project.board0811.dto.request;

import com.project.board0811.domain.Board;
import com.project.board0811.domain.User;
import com.project.board0811.domain.enums.BoardCategory;
import com.project.board0811.domain.enums.BoardStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateRequestDto {

    @NotBlank
    @Size(max = 50)
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private BoardCategory category = BoardCategory.FREE;

    // 기본값 PUBLIC (관리자 전용 입력으로 둘 수도 있음)
    private BoardStatus status = BoardStatus.PUBLIC;

    // 인증 사용자(User)를 주입받아 엔티티 생성
    public Board toEntity(User author) {
        return Board.builder()
                .author(author)
                .title(title)
                .content(content)
                .category(category != null ? category : BoardCategory.FREE)
                .status(status != null ? status : BoardStatus.PUBLIC)
                .build();
    }

}
