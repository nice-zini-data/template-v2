package com.zinidata.domain.requests.mapper;

import com.zinidata.domain.requests.vo.RequestFileVO;
import com.zinidata.domain.requests.vo.RequestVO;

import io.lettuce.core.dynamic.annotation.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 신규 설치 요청 Mapper 인터페이스
 * 
 * <p>신규 설치 요청 관련 데이터베이스 처리를 담당합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Mapper
public interface RequestMapper {
    
    /**
     * 요청 번호 조회
     * 
     * @return 요청 번호 (seq)
     */
    Long getRequestSeq();
    
    /**
     * 요청 등록
     * 
     * @param requestVo 요청 정보
     * @return 등록된 행 수
     */
    int insertRequest(RequestVO requestVo);
    
    /**
     * 여러 파일 정보 일괄 등록
     * 
     * @param fileList 파일 정보 목록
     * @return 등록된 행 수
     */
    int insertRequestImgs(List<RequestFileVO> fileList);

    /**
     * 요청 내역 조회(페이징 처리)
     * @param requestVO
     * @return
     * @throws Exception
     */
    List<RequestVO> selectRequestHistory(RequestVO requestVO) throws Exception;
    
    /**
     * 요청 상세 내역 조회
     * 
     * @param seq 요청 번호
     * @return 요청 상세 정보
     */
    RequestVO selectRequestHistoryDetail(@Param("seq") Long seq);
    
    /**
     * 요청 번호로 파일 목록 조회
     * 
     * @param seq 요청 번호
     * @return 파일 목록
     */
    List<RequestFileVO> selectRequestFilesBySeq(@Param("seq") Long seq, @Param("execSeq") Long execSeq);
    
    /**
     * 수행 내역 조회(페이징 처리)
     * @param requestVO
     * @return
     * @throws Exception
     */
    List<RequestVO> selectExecuteHistory(RequestVO requestVO) throws Exception;
    
    /**
     * 수행 내역 등록
     * 
     * @param requestVO 수행 내역 정보
     * @return 등록된 행 수
     */
    int insertExecute(RequestVO requestVO);
    
    /**
     * 수행 내역 수정
     * 
     * @param requestVO 수행 내역 정보
     * @return 수정된 행 수
     */
    int updateRequestStatus(RequestVO requestVO);
    
    /**
     * 수행 내역 업데이트 (완료 증빙)
     * 
     * @param requestVO 수행 내역 정보
     * @return 수정된 행 수
     */
    int updateExecute(RequestVO requestVO);
    
    /**
     * 요청 취소
     * 
     * @param requestVO 요청 정보 (seq, memNo 포함)
     * @return 취소된 행 수
     */
    int cancelRequest(RequestVO requestVO);

    /**
     * 수행 취소
     * @param requestVO
     * @return
     */
    int deleteExecute(RequestVO requestVO);

    /**
     * 파일명, 파일경로, 원본파일명 검증
     * 
     * @param requestFileVO 파일 정보
     * @return 파일 존재 여부
     */
    List<RequestFileVO> selectFileByFileNmFilePathOrgFileNm(RequestFileVO requestFileVO) throws Exception;
}

