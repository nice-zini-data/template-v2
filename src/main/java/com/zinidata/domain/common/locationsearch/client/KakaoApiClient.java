package com.zinidata.domain.common.locationsearch.client;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.domain.common.locationsearch.config.KakaoApiConfig;

/**
 * 카카오 API 호출 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiClient {
    
    private final KakaoApiConfig kakaoApiConfig;
    private final RestTemplate restTemplate;
    
    /**
     * 카카오 주소 검색 API 호출
     * 
     * @param query 검색 쿼리
     * @return 카카오 API 응답
     */
    public Map<String, Object> searchAddress(String query) throws ValidationException {
        try {
            String url = kakaoApiConfig.getApiUrl().getAddress() + "?query=" + encodeQuery(query);
            
            log.info("[LOCATION-V1] 카카오 주소 검색 API 호출: query={}", query);
            log.debug("[LOCATION-V1] 카카오 주소 검색 API URL: {}", url);
            
            HttpEntity<?> entity = createHttpEntity();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new ValidationException(Status.실패, "카카오 API 응답이 비어있습니다.");
            }
            
            log.debug("[LOCATION-V1] 카카오 주소 검색 API 응답: {}", body);
            return body;
            
        } catch (ValidationException e) {
            throw e; // 이미 변환된 예외는 그대로 전파
        } catch (Exception e) {
            log.error("[LOCATION-V1] 카카오 주소 검색 API 호출 실패: query={}", query, e);
            throw new ValidationException(Status.실패, "주소 검색 API 호출 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 카카오 키워드 검색 API 호출
     * 
     * @param query 검색 쿼리
     * @return 카카오 API 응답
     */
    public Map<String, Object> searchKeyword(String query) throws ValidationException {
        try {
            String url = kakaoApiConfig.getApiUrl().getKeyword() + "?query=" + encodeQuery(query);
            
            log.info("[LOCATION-V1] 카카오 키워드 검색 API 호출: query={}", query);
            log.debug("[LOCATION-V1] 카카오 키워드 검색 API URL: {}", url);
            
            HttpEntity<?> entity = createHttpEntity();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new ValidationException(Status.실패, "카카오 API 응답이 비어있습니다.");
            }
            
            log.debug("[LOCATION-V1] 카카오 키워드 검색 API 응답: {}", body);
            return body;
            
        } catch (ValidationException e) {
            throw e; // 이미 변환된 예외는 그대로 전파
        } catch (Exception e) {
            log.error("[LOCATION-V1] 카카오 키워드 검색 API 호출 실패: query={}", query, e);
            throw new ValidationException(Status.실패, "키워드 검색 API 호출 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 카카오 키워드 검색 API 호출 (반경 검색)
     * 
     * @param query 검색 쿼리
     * @param x 중심 경도 (longitude)
     * @param y 중심 위도 (latitude)
     * @param radius 반경 거리 (미터, 최대 20000)
     * @return 카카오 API 응답
     */
    public Map<String, Object> searchKeyword(String query, Double x, Double y, Integer radius) throws ValidationException {
        try {
            StringBuilder urlBuilder = new StringBuilder(kakaoApiConfig.getApiUrl().getKeyword());
            urlBuilder.append("?query=").append(encodeQuery(query));
            
            if (x != null && y != null) {
                urlBuilder.append("&x=").append(x);
                urlBuilder.append("&y=").append(y);
            }
            
            if (radius != null && radius > 0) {
                // 카카오 API 최대 반경 20000m 제한
                int validRadius = Math.min(radius, 20000);
                urlBuilder.append("&radius=").append(validRadius);
            }
            
            String url = urlBuilder.toString();
            
            log.info("[LOCATION-V1] 카카오 키워드 검색 API 호출 (반경 검색): query={}, x={}, y={}, radius={}", 
                    query, x, y, radius);
            log.debug("[LOCATION-V1] 카카오 키워드 검색 API URL: {}", url);
            
            HttpEntity<?> entity = createHttpEntity();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new ValidationException(Status.실패, "카카오 API 응답이 비어있습니다.");
            }
            
            log.debug("[LOCATION-V1] 카카오 키워드 검색 API 응답: {}", body);
            return body;
            
        } catch (ValidationException e) {
            throw e; // 이미 변환된 예외는 그대로 전파
        } catch (Exception e) {
            log.error("[LOCATION-V1] 카카오 키워드 검색 API 호출 실패: query={}, x={}, y={}, radius={}", 
                    query, x, y, radius, e);
            throw new ValidationException(Status.실패, "키워드 검색 API 호출 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 카카오 좌표→주소 변환 API 호출 (역지오코딩)
     * 
     * @param lat 위도
     * @param lng 경도
     * @return 카카오 API 응답
     */
    public Map<String, Object> coord2Address(double lat, double lng) throws ValidationException {
        try {
            // 좌표 유효성 검증
            if (lat < 33.0 || lat > 43.0 || lng < 124.0 || lng > 132.0) {
                throw new ValidationException(Status.실패, "유효하지 않은 좌표입니다. (한국 영역 벗어남)");
            }
            
            String url = String.format("%s?x=%f&y=%f&input_coord=WGS84", 
                kakaoApiConfig.getApiUrl().getCoord2address(), lng, lat);
            
            log.info("[LOCATION-V1] 카카오 좌표→주소 변환 API 호출: lat={}, lng={}", lat, lng);
            log.debug("[LOCATION-V1] 카카오 좌표→주소 변환 API URL: {}", url);
            
            HttpEntity<?> entity = createHttpEntity();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new ValidationException(Status.실패, "카카오 API 응답이 비어있습니다.");
            }
            
            log.debug("[LOCATION-V1] 카카오 좌표→주소 변환 API 응답: {}", body);
            return body;
            
        } catch (ValidationException e) {
            throw e; // 이미 변환된 예외는 그대로 전파
        } catch (Exception e) {
            log.error("[LOCATION-V1] 카카오 좌표→주소 변환 API 호출 실패: lat={}, lng={}", lat, lng, e);
            throw new ValidationException(Status.실패, "좌표→주소 변환 API 호출 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * HTTP 헤더 생성
     */
    private HttpEntity<?> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiConfig.getApiKey());
        return new HttpEntity<>(headers);
    }
    
    /**
     * 쿼리 문자열 인코딩
     */
    private String encodeQuery(String query) {
        return query.replace(" ", "+");
    }
} 