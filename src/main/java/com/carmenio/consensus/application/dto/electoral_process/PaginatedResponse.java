package com.carmenio.consensus.application.dto.electoral_process;

import lombok.*;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * <p>
 * Used for list endpoints to provide pagination metadata alongside content.
 *
 * @param <T> the type of items in the content list
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    /**
     * Returns whether this is the first page.
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Returns whether this is the last page.
     */
    public boolean isLast() {
        return page >= totalPages - 1;
    }
}
