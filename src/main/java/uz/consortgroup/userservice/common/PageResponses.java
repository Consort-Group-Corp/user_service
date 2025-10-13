package uz.consortgroup.userservice.common;

import org.springframework.data.domain.Page;
import uz.consortgroup.core.api.v1.dto.user.response.PageResponse;


import java.util.function.Function;

public final class PageResponses {
    private PageResponses() {}

    public static <T> PageResponse<T> from(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .hasNext(p.hasNext())
                .hasPrevious(p.hasPrevious())
                .build();
    }

    public static <S, T> PageResponse<T> map(Page<S> p, Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .content(p.getContent().stream().map(mapper).toList())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .hasNext(p.hasNext())
                .hasPrevious(p.hasPrevious())
                .build();
    }
}
