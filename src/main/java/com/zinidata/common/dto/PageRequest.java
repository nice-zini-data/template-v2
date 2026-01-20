package com.zinidata.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private int page = 1;
    
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private int size = 10;
    
    private String sort;
    private String direction = "DESC";
    
    private String search;
    private String searchType;
    
    // MyBatis용 offset 계산
    public int getOffset() {
        return (page - 1) * size;
    }
    
    // MyBatis용 limit 값
    public int getLimit() {
        return size;
    }
    
    // 정렬 방향 검증
    public String getDirection() {
        if (direction != null && direction.equalsIgnoreCase("ASC")) {
            return "ASC";
        }
        return "DESC";
    }
    
    // 검색 조건 존재 여부
    public boolean hasSearch() {
        return search != null && !search.trim().isEmpty();
    }
    
    // 정렬 조건 존재 여부
    public boolean hasSort() {
        return sort != null && !sort.trim().isEmpty();
    }
} 