package com.project.board0811.common.paging;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequestDto {

    @Min(0)
    private int page = 0;

    @Min(1) @Max(100)
    private int size = 20;

    private String sort = "createdAt"; // 필드명
    private String direction = "desc"; // asc | desc

    public org.springframework.data.domain.Pageable toPageable() {
        var sortObj = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.by(sort).descending()
                : org.springframework.data.domain.Sort.by(sort).ascending();
        return org.springframework.data.domain.PageRequest.of(page, size, sortObj);
    }
}
