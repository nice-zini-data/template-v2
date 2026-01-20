package com.zinidata.domain.home.mapper;

import com.zinidata.domain.home.vo.HomeStatsVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 홈 도메인 Mapper 인터페이스
 * 
 * <p>홈 페이지 관련 데이터베이스 조회 기능을 제공합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface HomeMapper {
    
    /**
     * 홈 통계 정보 조회
     * 
     * @param centerX 중심점 X 좌표 (경도)
     * @param centerY 중심점 Y 좌표 (위도)
     * @param radius 반경 (미터)
     * @param memNo 회원 번호
     * @return 홈 통계 정보
     */
    HomeStatsVO selectHomeStats(HomeStatsVO requestVo);
}
