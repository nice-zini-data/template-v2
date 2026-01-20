package com.zinidata.domain.requests.mapper;

import com.zinidata.domain.requests.vo.MapVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 맵 조회 Mapper 인터페이스
 * 
 * <p>맵 조회 관련 데이터베이스 처리를 담당합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface MapMapper {
    
    /**
     * 요청 맵 조회
     * 
     * @param mapVO 맵 조회 요청 정보
     * @return 맵 조회 결과 목록
     */
    List<MapVO> requestMap(MapVO mapVO);

    /**
     * 요청 맵 조회 (지역 단위 요청건 목록)
     */
    List<MapVO> requestMapAdmi(MapVO mapVO);
}

