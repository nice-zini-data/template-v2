package com.zinidata.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
    
    // 생성자 (편의용)
    public PageResponse(List<T> content, PageRequest pageRequest, long totalElements) {
        this.content = content;
        this.page = pageRequest.getPage();
        this.size = pageRequest.getSize();
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 1;
        this.last = page >= totalPages;
        this.empty = content == null || content.isEmpty();
    }
    
    // 빈 페이지 생성
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 1, 10, 0, 0, true, true, true);
    }
    
    // 단순 생성
    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        return new PageResponse<>(content, pageRequest, totalElements);
    }
    
    // 전체 리스트를 페이징하여 생성 (메모리 내 페이징용)
    public static <T> PageResponse<T> of(List<T> allContent, PageRequest pageRequest) {
        if (allContent == null || allContent.isEmpty()) {
            return empty();
        }
        
        int totalElements = allContent.size();
        int offset = pageRequest.getOffset();
        int limit = pageRequest.getLimit();
        
        // 범위 검증
        if (offset >= totalElements) {
            return new PageResponse<>(List.of(), pageRequest, totalElements);
        }
        
        int toIndex = Math.min(offset + limit, totalElements);
        List<T> content = allContent.subList(offset, toIndex);
        
        return new PageResponse<>(content, pageRequest, totalElements);
    }
    
    // 다음 페이지 존재 여부
    public boolean hasNext() {
        return !last;
    }
    
    // 이전 페이지 존재 여부
    public boolean hasPrevious() {
        return !first;
    }
    
    // 현재 페이지 요소 수
    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }
} 