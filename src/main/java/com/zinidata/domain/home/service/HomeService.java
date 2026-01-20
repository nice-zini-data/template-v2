package com.zinidata.domain.home.service;

import com.zinidata.domain.home.mapper.HomeMapper;
import com.zinidata.domain.home.vo.HomeStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 홈 도메인 서비스
 * 
 * <p>홈 페이지 관련 비즈니스 로직을 제공합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {
    
    private final HomeMapper homeMapper;
    
    /**
     * 홈 통계 정보 조회
     * 
     * @param requestVo 홈 통계 조회 요청 정보 (centerX, centerY, radius, memNo 포함)
     * @return 홈 통계 정보
     */
    public HomeStatsVO getHomeStats(HomeStatsVO requestVo) {
        log.info("[HOME_SERVICE] 홈 통계 정보 조회 - centerX: {}, centerY: {}, radius: {}, memNo: {}", 
                requestVo.getCenterX(), requestVo.getCenterY(), requestVo.getRadius(), requestVo.getMemNo());
        
        try {
            HomeStatsVO result = homeMapper.selectHomeStats(requestVo);
            
            if (result == null) {
                log.warn("[HOME_SERVICE] 홈 통계 정보 조회 결과가 null입니다.");
                return HomeStatsVO.builder()
                        .radiusCnt(0)
                        .todayCnt(0)
                        .requestCnt(0)
                        .execCnt(0)
                        .build();
            }
            
            log.info("[HOME_SERVICE] 홈 통계 정보 조회 완료 - radiusCnt: {}, todayCnt: {}, requestCnt: {}, execCnt: {}", 
                    result.getRadiusCnt(), result.getTodayCnt(), result.getRequestCnt(), result.getExecCnt());
            
            return result;
            
        } catch (Exception e) {
            log.error("[HOME_SERVICE] 홈 통계 정보 조회 중 오류 발생", e);
            throw new RuntimeException("홈 통계 정보 조회 중 오류가 발생했습니다.", e);
        }
    }
}
