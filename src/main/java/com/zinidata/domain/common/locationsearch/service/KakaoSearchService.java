package com.zinidata.domain.common.locationsearch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.domain.common.locationsearch.client.KakaoApiClient;

/**
 * 카카오 검색 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSearchService {
    
    private final KakaoApiClient kakaoApiClient;
    
    /**
     * 주소 검색
     * 
     * @param query 검색 쿼리
     * @return 표준화된 응답
     */
    public Map<String, Object> searchByAddress(String query) throws ValidationException {
        log.info("[LOCATION-V1] 주소 검색 처리 시작: query={}", query);
        
        try {
            // 1. 입력값 검증
            validateSearchQuery(query);
            
            // 2. 카카오 API 호출
            Map<String, Object> kakaoResponse = kakaoApiClient.searchAddress(query.trim());
            
            // 3. 응답 표준화
            Map<String, Object> result = createStandardResponse(kakaoResponse, "address");
            
            log.info("[LOCATION-V1] 주소 검색 완료: query={}", query);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[LOCATION-V1] 주소 검색 검증 실패: query={}, reason={}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[LOCATION-V1] 주소 검색 처리 오류: query={}", query, e);
            throw new ValidationException(Status.실패, "주소 검색 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 키워드 검색
     * 
     * @param query 검색 쿼리
     * @return 표준화된 응답
     */
    public Map<String, Object> searchByKeyword(String query) throws ValidationException {
        log.info("[LOCATION-V1] 키워드 검색 처리 시작: query={}", query);
        
        try {
            // 1. 입력값 검증
            validateSearchQuery(query);
            
            // 2. 카카오 API 호출
            Map<String, Object> kakaoResponse = kakaoApiClient.searchKeyword(query.trim());
            
            // 3. 응답 표준화
            Map<String, Object> result = createStandardResponse(kakaoResponse, "keyword");
            
            log.info("[LOCATION-V1] 키워드 검색 완료: query={}", query);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[LOCATION-V1] 키워드 검색 검증 실패: query={}, reason={}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[LOCATION-V1] 키워드 검색 처리 오류: query={}", query, e);
            throw new ValidationException(Status.실패, "키워드 검색 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 키워드 검색 (반경 검색)
     * 
     * @param query 검색 쿼리
     * @param x 중심 경도 (longitude)
     * @param y 중심 위도 (latitude)
     * @param radius 반경 거리 (미터)
     * @return 표준화된 응답
     */
    public Map<String, Object> searchByKeyword(String query, Double x, Double y, Integer radius) throws ValidationException {
        log.info("[LOCATION-V1] 키워드 검색 처리 시작 (반경 검색): query={}, x={}, y={}, radius={}", query, x, y, radius);
        
        try {
            // 1. 입력값 검증
            validateSearchQuery(query);
            
            // 2. 좌표 검증 (제공된 경우)
            if (x != null && y != null) {
                validateCoordinates(y, x); // y=위도, x=경도
            }
            
            // 3. 반경 검증
            if (radius != null && radius <= 0) {
                throw new ValidationException(Status.파라미터오류, "반경은 0보다 큰 값이어야 합니다.");
            }
            
            // 4. 카카오 API 호출
            Map<String, Object> kakaoResponse = kakaoApiClient.searchKeyword(query.trim(), x, y, radius);
            
            // 5. 응답 표준화
            Map<String, Object> result = createStandardResponse(kakaoResponse, "keyword");
            
            log.info("[LOCATION-V1] 키워드 검색 완료 (반경 검색): query={}", query);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[LOCATION-V1] 키워드 검색 검증 실패: query={}, reason={}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[LOCATION-V1] 키워드 검색 처리 오류: query={}", query, e);
            throw new ValidationException(Status.실패, "키워드 검색 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 통합 검색 (주소 + 키워드)
     * 
     * @param query 검색 쿼리
     * @return 표준화된 응답 (Controller에서 ApiResponse로 래핑됨)
     */
    public Map<String, Object> searchMixed(String query) throws ValidationException {
        log.info("[LOCATION-V1] 통합 검색 처리 시작: query={}", query);
        
        try {
            // 1. 입력값 검증
            validateSearchQuery(query);
            
            // 2. 주소 검색 시도
            Map<String, Object> addressResponse = kakaoApiClient.searchAddress(query.trim());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> addressDocuments = (List<Map<String, Object>>) addressResponse.get("documents");
            
            // 3. null 체크 및 초기화
            if (addressDocuments == null) {
                addressDocuments = new ArrayList<>();
            }
            
            // 4. 키워드 검색 시도
            Map<String, Object> keywordResponse = kakaoApiClient.searchKeyword(query.trim());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> keywordDocuments = (List<Map<String, Object>>) keywordResponse.get("documents");
            
            // 5. null 체크 및 초기화
            if (keywordDocuments == null) {
                keywordDocuments = new ArrayList<>();
            }
            
            // 6. 결과 통합 - Controller에서 ApiResponse로 래핑되므로 data 중첩 방지
            Map<String, Object> result = new HashMap<>();
            result.put("query", query.trim());
            result.put("addressResults", addressDocuments);
            result.put("keywordResults", keywordDocuments);
            result.put("totalCount", addressDocuments.size() + keywordDocuments.size());
            
            log.info("[LOCATION-V1] 통합 검색 완료: query={}, 주소 결과={}, 키워드 결과={}", 
                    query, addressDocuments.size(), keywordDocuments.size());
            
            return result;
            
        } catch (ValidationException e) {
            log.warn("[LOCATION-V1] 통합 검색 검증 실패: query={}, reason={}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[LOCATION-V1] 통합 검색 처리 오류: query={}", query, e);
            throw new ValidationException(Status.실패, "통합 검색 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 통합 검색 (주소 + 키워드, 반경 검색)
     * 
     * @param query 검색 쿼리
     * @param x 중심 경도 (longitude)
     * @param y 중심 위도 (latitude)
     * @param radius 반경 거리 (미터)
     * @return 표준화된 응답 (Controller에서 ApiResponse로 래핑됨)
     */
    public Map<String, Object> searchMixed(String query, Double x, Double y, Integer radius) throws ValidationException {
        log.info("[LOCATION-V1] 통합 검색 처리 시작 (반경 검색): query={}, x={}, y={}, radius={}", query, x, y, radius);
        
        try {
            // 1. 입력값 검증
            validateSearchQuery(query);
            
            // 2. 좌표 검증 (제공된 경우)
            if (x != null && y != null) {
                validateCoordinates(y, x); // y=위도, x=경도
            }
            
            // 3. 반경 검증
            if (radius != null && radius <= 0) {
                throw new ValidationException(Status.파라미터오류, "반경은 0보다 큰 값이어야 합니다.");
            }
            
            // 4. 주소 검색 시도
            Map<String, Object> addressResponse = kakaoApiClient.searchAddress(query.trim());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> addressDocuments = (List<Map<String, Object>>) addressResponse.get("documents");
            
            // 5. null 체크 및 초기화
            if (addressDocuments == null) {
                addressDocuments = new ArrayList<>();
            }
            
            // 6. 키워드 검색 시도 (반경 검색 적용)
            Map<String, Object> keywordResponse = kakaoApiClient.searchKeyword(query.trim(), x, y, radius);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> keywordDocuments = (List<Map<String, Object>>) keywordResponse.get("documents");
            
            // 7. null 체크 및 초기화
            if (keywordDocuments == null) {
                keywordDocuments = new ArrayList<>();
            }
            
            // 8. 결과 통합 - Controller에서 ApiResponse로 래핑되므로 data 중첩 방지
            Map<String, Object> result = new HashMap<>();
            result.put("query", query.trim());
            result.put("addressResults", addressDocuments);
            result.put("keywordResults", keywordDocuments);
            result.put("totalCount", addressDocuments.size() + keywordDocuments.size());
            
            log.info("[LOCATION-V1] 통합 검색 완료 (반경 검색): query={}, 주소 결과={}, 키워드 결과={}", 
                    query, addressDocuments.size(), keywordDocuments.size());
            
            return result;
            
        } catch (ValidationException e) {
            log.warn("[LOCATION-V1] 통합 검색 검증 실패: query={}, reason={}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[LOCATION-V1] 통합 검색 처리 오류: query={}", query, e);
            throw new ValidationException(Status.실패, "통합 검색 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 좌표→주소 변환 (역지오코딩)
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 표준화된 응답
     */
    public Map<String, Object> coord2Address(double lat, double lng) throws ValidationException {
        log.info("[LOCATION-V1] 좌표→주소 변환 처리 시작: lat={}, lng={}", lat, lng);
        
        try {
            // 1. 좌표 유효성 검증
            validateCoordinates(lat, lng);
            
            // 2. 카카오 API 호출
            Map<String, Object> kakaoResponse = kakaoApiClient.coord2Address(lat, lng);
            
            // 3. 응답 표준화
            Map<String, Object> result = createCoord2AddressResponse(kakaoResponse, lat, lng);
            
            log.info("[LOCATION-V1] 좌표→주소 변환 완료: lat={}, lng={}", lat, lng);
            return result;
            
        } catch (ValidationException e) {
            log.warn("[LOCATION-V1] 좌표→주소 변환 검증 실패: lat={}, lng={}, reason={}", lat, lng, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[LOCATION-V1] 좌표→주소 변환 처리 오류: lat={}, lng={}", lat, lng, e);
            throw new ValidationException(Status.실패, "좌표→주소 변환 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 카카오 API 응답을 표준화된 형태로 변환
     * 
     * <p>카카오 API의 원시 응답 데이터를 받아서 프론트엔드에서 사용하기 편한 형태로 변환합니다.
     * Controller에서 ApiResponse로 한 번 더 래핑되므로 여기서는 data 부분만 반환합니다.</p>
     * 
     * <p><strong>변환 과정:</strong></p>
     * <ul>
     *   <li>카카오 API의 documents 배열을 results로 매핑</li>
     *   <li>검색 타입 정보 추가 (address/keyword)</li>
     *   <li>전체 결과 개수 계산 및 추가</li>
     *   <li>카카오 API의 메타데이터 포함</li>
     * </ul>
     * 
     * @param kakaoResponse 카카오 API 원시 응답 데이터 (documents, meta 포함)
     * @param searchType 검색 타입 ("address" 또는 "keyword")
     * @return 표준화된 응답 데이터 (Controller에서 ApiResponse.data로 사용됨)
     * 
     * @see KakaoSearchApiController#searchByAddress(String)
     * @see KakaoSearchApiController#searchByKeyword(String)
     */
    private Map<String, Object> createStandardResponse(Map<String, Object> kakaoResponse, String searchType) {
        // 카카오 API 응답에서 documents와 meta 추출
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> documents = (List<Map<String, Object>>) kakaoResponse.get("documents");
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) kakaoResponse.get("meta");
        
        // 표준화된 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("searchType", searchType);    // 검색 타입 (address/keyword)
        result.put("results", documents);        // 검색 결과 목록
        result.put("totalCount", documents.size()); // 전체 결과 개수
        result.put("meta", meta);                // 카카오 API 메타데이터
        
        log.info("[LOCATION-V1] {} 검색 완료: 결과 수={}", searchType, documents.size());
        
        return result;
    }
    
    /**
     * 좌표→주소 변환 응답 생성
     * 
     * @param kakaoResponse 카카오 API 응답
     * @param lat 위도
     * @param lng 경도
     * @return 표준화된 응답
     */
    private Map<String, Object> createCoord2AddressResponse(Map<String, Object> kakaoResponse, double lat, double lng) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> documents = (List<Map<String, Object>>) kakaoResponse.get("documents");
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) kakaoResponse.get("meta");
        
        Map<String, Object> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lng", lng);
        result.put("addresses", documents != null ? documents : new ArrayList<>());
        result.put("totalCount", documents != null ? documents.size() : 0);
        result.put("meta", meta);
        
        // 주요 주소 정보 추출 (첫 번째 결과 사용)
        if (documents != null && !documents.isEmpty()) {
            Map<String, Object> firstDocument = documents.get(0);
            
            // 도로명 주소 정보 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> roadAddress = (Map<String, Object>) firstDocument.get("road_address");
            
            // 지번 주소 정보 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> address = (Map<String, Object>) firstDocument.get("address");
            
            if (roadAddress != null) {
                result.put("mainAddress", roadAddress.get("address_name"));
                result.put("roadAddress", roadAddress.get("address_name"));
                result.put("region1", roadAddress.get("region_1depth_name"));
                result.put("region2", roadAddress.get("region_2depth_name"));
                result.put("region3", roadAddress.get("region_3depth_name"));
            } else if (address != null) {
                result.put("mainAddress", address.get("address_name"));
                result.put("jibunAddress", address.get("address_name"));
                result.put("region1", address.get("region_1depth_name"));
                result.put("region2", address.get("region_2depth_name"));
                result.put("region3", address.get("region_3depth_name"));
            }
        }
        
        log.debug("[LOCATION-V1] 좌표→주소 변환 응답 생성: 결과 수={}", documents != null ? documents.size() : 0);
        
        return result;
    }
    
    /**
     * 좌표 유효성 검증
     * 
     * @param lat 위도
     * @param lng 경도
     * @throws ValidationException 검증 실패 시
     */
    private void validateCoordinates(double lat, double lng) throws ValidationException {
        // 위도 범위 검증 (한국 영역)
        if (lat < 33.0 || lat > 43.0) {
            throw new ValidationException(Status.파라미터오류, 
                String.format("위도가 유효하지 않습니다. (현재: %.6f, 유효범위: 33.0~43.0)", lat));
        }
        
        // 경도 범위 검증 (한국 영역)
        if (lng < 124.0 || lng > 132.0) {
            throw new ValidationException(Status.파라미터오류, 
                String.format("경도가 유효하지 않습니다. (현재: %.6f, 유효범위: 124.0~132.0)", lng));
        }
    }
    
    /**
     * 검색어 입력값 검증
     * 
     * @param query 검색어
     * @throws ValidationException 검증 실패 시
     */
    private void validateSearchQuery(String query) throws ValidationException {
        // 1. 입력값 검증
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException(Status.파라미터오류, "검색어를 입력해주세요.");
        }
        
        // 2. 길이 검증 (1~100자)
        String trimmedQuery = query.trim();
        if (trimmedQuery.length() > 100) {
            throw new ValidationException(Status.파라미터오류, "검색어는 100자 이하로 입력해주세요.");
        }
        
        // 3. 특수문자 검증 (SQL Injection 방지)
        if (trimmedQuery.contains("'") || trimmedQuery.contains("\"") || 
            trimmedQuery.contains(";") || trimmedQuery.contains("--")) {
            throw new ValidationException(Status.파라미터오류, "검색어에 특수문자는 사용할 수 없습니다.");
        }
    }

} 