package com.project.board0811.common.paging;

import java.util.function.Function;
import org.springframework.data.domain.Page;

public final class PageConverters {
    private PageConverters() {}

    public static <S, T> PageResponse<T> toResponse(
            Page<S> page, Function<S, T> mapper, String sort, String direction) {

        return PageResponse.<T>builder()
                .content(page.map(mapper).getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .sort(sort)
                .direction(direction)
                .build();
    }
}
