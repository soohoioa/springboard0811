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
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    /** QueryDSL 핵심 진입점. 스프링 빈으로 등록된 EntityManager를 내부적으로 사용 */
    private final JPAQueryFactory queryFactory;

    /**
     * 루트 댓글(= parent IS NULL) 페이지 조회
     */
    @Override
    public Page<Comment> findRootPage(Long boardId, Pageable pageable) {
        // 1) 실제 데이터 조회용 기본 쿼리 (루트 댓글: parent is null)
        var baseQuery = queryFactory
                .selectFrom(comment)
                .where(
                        comment.board.id.eq(boardId),
                        comment.parent.isNull()
                );

        // 2) 카운트 쿼리 (QueryDSL 5: fetchCount 대신 count 분리)
        //    fetchOne()은 null 가능 → Long으로 받고 0L로 안전 변환
        Long totalBoxed = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.board.id.eq(boardId),
                        comment.parent.isNull()
                )
                .fetchOne();
        long total = (totalBoxed != null) ? totalBoxed : 0L;

        // 3) 정렬 변환 (Spring Sort → QueryDSL OrderSpecifier)
        List<OrderSpecifier<?>> orders = toOrderSpecifiers(pageable.getSort());

        // 4) orderBy 가변 인자에 안전하게 전달하기 위해 항상 배열 형태로 통일
        OrderSpecifier<?>[] orderSpecifiers = orders.isEmpty()
                ? new OrderSpecifier<?>[]{ new OrderSpecifier<>(Order.ASC, comment.createdAt) } // 기본 정렬: 작성시각 오름차순
                : orders.toArray(new OrderSpecifier<?>[0]);

        // 5) 페이징 + 정렬 적용하여 루트 댓글 목록 조회
        List<Comment> content = baseQuery
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 6) PageImpl 로 포장하여 반환
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 여러 루트 댓글 id들(parentIds)에 달린 자식(답글)들을 한 번에 조회하고,
     * parentId -> List<Comment> 형태로 그룹핑하여 반환
     */
    @Override
    public Map<Long, List<Comment>> findRepliesGroupedByParentIds(Collection<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) return Collections.emptyMap();

        // 1) parent.id IN (...) 으로 자식(답글) 일괄 조회
        List<Comment> replies = queryFactory
                .selectFrom(comment)
                .where(comment.parent.id.in(parentIds))
                .orderBy(comment.parent.id.asc(), comment.createdAt.asc())
                .fetch();

        // 2) parentId 기준으로 그룹핑하여 Map 구성 (입력 순서 유지)
        return replies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParent().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Spring Data의 Sort 정보를 QueryDSL의 OrderSpecifier 리스트로 변환
     */
    private List<OrderSpecifier<?>> toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order s : sort) {
            // Spring Sort → QueryDSL Order 로 치환
            Order direction = s.isAscending() ? Order.ASC : Order.DESC;

            // 허용된 정렬 필드만 변환
            switch (s.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, comment.createdAt));
                case "id"        -> orders.add(new OrderSpecifier<>(direction, comment.id));
                default -> { /* 미허용/미지원 필드는 무시 (보안/안정성상 안전) */ }
            }
        }
        return orders;
    }

}
