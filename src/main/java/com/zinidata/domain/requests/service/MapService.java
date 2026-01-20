package com.zinidata.domain.requests.service;

import com.zinidata.common.util.AesCryptoUtil;
import com.zinidata.domain.requests.mapper.MapMapper;
import com.zinidata.domain.requests.vo.MapVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 맵 조회 서비스
 * 
 * <p>맵 조회 관련 비즈니스 로직을 제공합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {
    
    private final MapMapper mapMapper;
    
    /**
     * 요청 맵 조회
     * 
     * @param mapVO 맵 조회 요청 정보
     * @return 맵 조회 결과 목록
     */
    public Map<String, Object> getRequestMap(MapVO mapVO) {
        log.info("[MAP] 요청 맵 조회 시작 - gubun: {}, minx: {}, miny: {}, maxx: {}, maxy: {}", 
                mapVO.getGubun(), mapVO.getMinx(), mapVO.getMiny(), mapVO.getMaxx(), mapVO.getMaxy());
        
        try {
            // 구분 검증
            if (mapVO.getGubun() == null || (!mapVO.getGubun().equals("block") && !mapVO.getGubun().equals("admi") && 
                    !mapVO.getGubun().equals("cty") && !mapVO.getGubun().equals("mega"))) {
                log.warn("[MAP] 잘못된 구분 값: {}", mapVO.getGubun());
                throw new IllegalArgumentException("구분 값은 block, admi, cty, mega 중 하나여야 합니다.");
            }
            
            // 좌표 검증
            if (mapVO.getMinx() == null || mapVO.getMiny() == null || 
                    mapVO.getMaxx() == null || mapVO.getMaxy() == null) {
                log.warn("[MAP] 좌표 값이 없습니다.");
                throw new IllegalArgumentException("좌표 값은 필수입니다.");
            }
            
            List<MapVO> result = mapMapper.requestMap(mapVO);
            List<MapVO> list = null;
            if(!mapVO.getGubun().equals("block")){
                list = mapMapper.requestMapAdmi(mapVO);

                for(MapVO vo : list){
                    if (vo.getSeq() != null) {
                        vo.setEncryptedSeq(AesCryptoUtil.encrypt(String.valueOf(vo.getSeq())));
                    }
                }
            }else{
                for(MapVO vo : result){
                    if (vo.getSeq() != null) {
                        vo.setEncryptedSeq(AesCryptoUtil.encrypt(String.valueOf(vo.getSeq())));
                    }
                }
            }
            
            // 결과 구성
            Map<String, Object> map = new HashMap<>();
            map.put("result", result);
            map.put("count", result != null ? result.size() : 0);
            map.put("list", list);

            log.info("[MAP] 요청 맵 조회 완료 - 조회 건수: {}", result != null ? result.size() : 0);
            return map;
            
        } catch (Exception e) {
            log.error("[MAP] 요청 맵 조회 중 오류 발생", e);
            throw new RuntimeException("요청 맵 조회 중 오류가 발생했습니다.", e);
        }
    }
}

