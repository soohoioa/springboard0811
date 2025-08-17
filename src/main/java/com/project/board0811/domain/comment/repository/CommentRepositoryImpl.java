package com.project.board0811.domain.comment.repository;

import static com.project.board0811.domain.comment.entity.QComment.comment;

import com.project.board0811.domain.comment.entity.Comment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Comment> findRootPage(Long boardId, Pageable pageable) {
        // base query (루트 댓글: parent is null)
        var baseQuery = queryFactory
                .selectFrom(comment)
                .where(
                        comment.board.id.eq(boardId),
                        comment.parent.isNull()
                );

        // total count (fetchOne()이 null일 수 있으므로 NPE 방지)
        Long totalBoxed = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.board.id.eq(boardId),
                        comment.parent.isNull()
                )
                .fetchOne();
        long total = (totalBoxed != null) ? totalBoxed : 0L;

        // 정렬 변환
        List<OrderSpecifier<?>> orders = toOrderSpecifiers(pageable.getSort());

        // 삼항 연산자에서 타입 불일치 방지 → 배열로 통일
        OrderSpecifier<?>[] orderSpecifiers = orders.isEmpty()
                ? new OrderSpecifier<?>[]{ new OrderSpecifier<>(Order.ASC, comment.createdAt) }
                : orders.toArray(new OrderSpecifier<?>[0]);

        // 페이징 + 정렬 적용
        List<Comment> content = baseQuery
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Map<Long, List<Comment>> findRepliesGroupedByParentIds(Collection<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) return Collections.emptyMap();

        List<Comment> replies = queryFactory
                .selectFrom(comment)
                .where(comment.parent.id.in(parentIds))
                .orderBy(comment.parent.id.asc(), comment.createdAt.asc())
                .fetch();

        return replies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParent().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private List<OrderSpecifier<?>> toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order s : sort) { // 이름 충돌 회피
            Order direction = s.isAscending() ? Order.ASC : Order.DESC;
            switch (s.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, comment.createdAt));
                case "id"        -> orders.add(new OrderSpecifier<>(direction, comment.id));
                // 필요시 정렬 필드 추가
                default -> { /* no-op */ }
            }
        }
        return orders;
    }

}
