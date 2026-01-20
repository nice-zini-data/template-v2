package com.zinidata.domain.requests.service;

import com.zinidata.common.enums.Status;
import com.zinidata.common.exception.ValidationException;
import com.zinidata.common.util.AesCryptoUtil;
import com.zinidata.common.util.ImageResizeUtil;
import com.zinidata.domain.common.auth.mapper.AuthMapper;
import com.zinidata.domain.common.auth.vo.MemberVO;
import com.zinidata.domain.common.sms.service.UnifiedSmsService;
import com.zinidata.domain.requests.mapper.RequestMapper;
import com.zinidata.domain.requests.vo.RequestFileVO;
import com.zinidata.domain.requests.vo.RequestVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 신규 설치 요청 서비스
 * 
 * <p>신규 설치 요청 등록 관련 비즈니스 로직을 제공합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {
    
    private final RequestMapper requestMapper;
    private final UnifiedSmsService unifiedSmsService;
    private final AuthMapper authMapper;
    
    @Value("${app.code:NBZM}")
    private String appCode;
    
    @Value("${custom.upload.base-path}")
    private String basePath;
    
    @Value("${custom.upload.create-date-folder:true}")
    private boolean createDateFolder;
    
    @Value("${custom.upload.max-image-size:1048576}") // 기본값: 1MB (바이트 단위)
    private long maxImageSize;

    /**
     * 신규 설치 요청 등록
     * 
     * @param requestVo 요청 정보
     * @return 등록된 요청 정보
     */
    @Transactional
    public RequestVO registerRequest(RequestVO requestVo) {
        log.info("[REQUEST] 신규/AS 요청 등록 시작 - crtName: {}, crtPhoneNumber: {}", 
                requestVo.getCrtName(), requestVo.getCrtPhoneNumber());
        
        try {
            // 요청 번호 조회
            Long seq = requestMapper.getRequestSeq();
            if (seq == null) {
                log.error("[REQUEST] 요청 번호 조회 실패");
                requestVo.setSuccess(false);
                throw new RuntimeException("요청 번호 조회에 실패했습니다.");
            }
            
            // seq 설정
            requestVo.setSeq(seq);
            requestVo.setEncryptedSeq(AesCryptoUtil.encrypt(String.valueOf(seq)));
            requestVo.setCrtId(requestVo.getMemNo());
            requestVo.setCrtName(AesCryptoUtil.encrypt(requestVo.getCrtName()));


            // 휴대폰 번호 위변조 못하게, 세션에 있는 회원번호로 회원 DB조회해서 mobileNo 가져와서 getCrtPhoneNumber 에 추가하기
            if (requestVo.getMemNo() != null) {
                // 회원번호로 회원 정보 조회
                MemberVO member = authMapper.findByMemNo(requestVo.getMemNo(), appCode);
                if (member != null && member.getMobileNo() != null) {
                    // DB에서 조회한 회원의 mobileNo를 설정 (위변조 방지)
                    // mobileNo는 이미 암호화되어 있으므로 그대로 사용
                    requestVo.setCrtPhoneNumber(member.getMobileNo());
                    log.info("[REQUEST] 회원 DB에서 mobileNo 조회하여 설정 - memNo: {}", requestVo.getMemNo());
                } else {
                    // 회원 정보가 없거나 mobileNo가 없는 경우 클라이언트에서 받은 값 사용 (하위 호환성)
                    log.warn("[REQUEST] 회원 정보 조회 실패 또는 mobileNo 없음 - memNo: {}, 클라이언트 값 사용", requestVo.getMemNo());
                    requestVo.setCrtPhoneNumber(AesCryptoUtil.encrypt(requestVo.getPhone()));
                }
            } else {
                // memNo가 없는 경우 클라이언트에서 받은 값 사용 (하위 호환성)
                log.warn("[REQUEST] memNo가 없어 클라이언트에서 받은 phone 값 사용");
                requestVo.setCrtPhoneNumber(AesCryptoUtil.encrypt(requestVo.getPhone()));
            }

            // 요청 등록
            int result = requestMapper.insertRequest(requestVo);
            
            if (result > 0) {
                if(requestVo.getServiceGb().equals("0")) {
                    log.info("[REQUEST] 신규 설치 요청 등록 성공 - requestNo: {}", requestVo.getRequestNo());
                    requestVo.setSuccess(true);
                } else {
                    log.info("[REQUEST] A/S 요청 등록 성공 - requestNo: {}", requestVo.getRequestNo());
                    requestVo.setSuccess(true);
                }
            } else {
                if(requestVo.getServiceGb().equals("0")) {
                    log.warn("[REQUEST] 신규 설치 요청 등록 실패");
                    requestVo.setSuccess(false);
                } else {
                    log.warn("[REQUEST] A/S 요청 등록 실패");
                    requestVo.setSuccess(false);
                }
                requestVo.setSuccess(false);
            }
            
            return requestVo;
            
        } catch (Exception e) {
            if(requestVo.getServiceGb().equals("0")) {
                log.error("[REQUEST] 신규 설치 요청 등록 중 오류 발생", e);
                requestVo.setSuccess(false);
            } else {
                log.error("[REQUEST] A/S 요청 등록 중 오류 발생", e);
                requestVo.setSuccess(false);
            }
            throw new RuntimeException("신규/AS 요청 등록 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 파일 업로드 및 정보 등록
     * 
     * @param files 업로드할 파일들
     * @param seq 요청 번호
     * @param crtId 생성자 ID
     * @param serviceGb 서비스 구분
     * @param executeSw 실행 여부
     * @return 등록된 파일 정보 목록
     */
    @Transactional
    public List<RequestFileVO> uploadFiles(List<MultipartFile> files, String requestSeq, Long crtId, String serviceGb, String executeSw) {
        log.info("[REQUEST] 파일 업로드 시작 - seq: {}, crtId: {}, serviceGb: {}, executeSw: {}, fileCount: {}", 
        requestSeq, crtId, serviceGb, executeSw, files.size());
        
        List<RequestFileVO> fileVoList = new ArrayList<>();
        
        try {
            // 업로드 디렉토리 생성
            String uploadDir = getUploadDirectory();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    log.warn("[REQUEST] 빈 파일 건너뜀: {}", file.getOriginalFilename());
                    continue;
                }
                
                // 이미지 파일인 경우 최대 크기 체크 및 자동 리사이징
                MultipartFile processedFile = file;
                String contentType = file.getContentType();
                String fileName = file.getOriginalFilename();
                long originalSize = file.getSize();
                
                // 파일 정보 로깅 (모든 파일)
                log.info("[REQUEST] 파일 업로드 처리 시작 - 파일명: {}, 타입: {}, 크기: {} bytes ({} MB)", 
                        fileName, contentType, originalSize, String.format("%.2f", originalSize / 1024.0 / 1024.0));
                
                if (contentType != null && contentType.startsWith("image/")) {
                    log.info("[REQUEST] ========== 이미지 파일 리사이징 체크 시작 ==========");
                    log.info("[REQUEST] 이미지 파일 정보 - 파일명: {}, 타입: {}, 원본 크기: {} bytes ({} MB), 최대 허용 크기: {} bytes ({} MB)", 
                            fileName, contentType, originalSize, String.format("%.2f", originalSize / 1024.0 / 1024.0),
                            maxImageSize, String.format("%.2f", maxImageSize / 1024.0 / 1024.0));
                    
                    // 이미지가 최대 크기를 넘으면 자동 리사이징
                    processedFile = ImageResizeUtil.resizeImageIfNeeded(file, maxImageSize);
                    
                    long processedSize = processedFile.getSize();
                    boolean wasResized = processedSize != originalSize;
                    
                    if (wasResized) {
                        long sizeReduction = originalSize - processedSize;
                        double reductionPercent = (double) sizeReduction / originalSize * 100.0;
                        log.info("[REQUEST] ✅ 이미지 리사이징 성공!");
                        log.info("[REQUEST] 리사이징 결과:");
                        log.info("[REQUEST]   - 원본 크기: {} bytes ({} MB)", originalSize, String.format("%.2f", originalSize / 1024.0 / 1024.0));
                        log.info("[REQUEST]   - 리사이징 크기: {} bytes ({} MB)", processedSize, String.format("%.2f", processedSize / 1024.0 / 1024.0));
                        log.info("[REQUEST]   - 크기 감소: {} bytes ({} MB, {}%)", 
                                sizeReduction, String.format("%.2f", sizeReduction / 1024.0 / 1024.0), String.format("%.2f", reductionPercent));
                    } else {
                        log.info("[REQUEST] ℹ️ 이미지 리사이징 불필요 - 파일 크기가 적절함 ({} bytes, {} MB)", 
                                originalSize, String.format("%.2f", originalSize / 1024.0 / 1024.0));
                    }
                    log.info("[REQUEST] ========== 이미지 파일 리사이징 체크 완료 ==========");
                } else {
                    log.info("[REQUEST] 비이미지 파일 - 리사이징 없이 원본 그대로 사용");
                }
                
                // 원본 파일명
                String orgFileNm = processedFile.getOriginalFilename();
                if (orgFileNm == null) {
                    orgFileNm = "unknown";
                }
                
                // 파일명 생성 (UUID)
                String fileNm = UUID.randomUUID().toString() + getFileExtension(orgFileNm);
                
                // 파일 저장 경로
                String filePath = uploadDir + File.separator + fileNm;
                
                // 파일 저장
                Path filePathObj = Paths.get(filePath);
                Files.copy(processedFile.getInputStream(), filePathObj);
                
                log.info("[REQUEST] 파일 저장 완료 - filePath: {}, size: {} bytes", filePath, processedFile.getSize());
                
                Long seq = Long.parseLong(requestSeq);

                // 파일 정보 VO 생성
                RequestFileVO fileVo = RequestFileVO.builder()
                        .seq(seq)
                        .crtId(crtId)
                        .serviceGb(serviceGb)
                        .executeSw(executeSw != null ? executeSw : "0") // 파라미터에서 받은 값, 없으면 기본값: 미실행
                        .fileNm(fileNm)
                        .filePath(filePath)
                        .orgFileNm(orgFileNm)
                        .fileSize(processedFile.getSize())
                        .status("1") // 기본값: 활성
                        .crtDt(LocalDateTime.now())
                        .build();
                
                fileVoList.add(fileVo);
            }
            
            // 파일 정보 일괄 등록
            if (!fileVoList.isEmpty()) {
                int result = requestMapper.insertRequestImgs(fileVoList);
                log.info("[REQUEST] 파일 정보 등록 완료 - 등록 건수: {}", result);
            }
            
            return fileVoList;
            
        } catch (IOException e) {
            log.error("[REQUEST] 파일 업로드 중 오류 발생", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 요청 내역 목록조회(페이징 처리)
     * 
     * @param requestVO 조회 조건 VO
     * @return 요청 내역 목록
     */
    public List<RequestVO> getRequestHistory(RequestVO requestVO) {
        log.info("[REQUEST] 요청 내역 조회 시작 - searchText: {}, pageNo: {}, size: {}, sortType: {}, centerX: {}, centerY: {}", 
                requestVO.getSearchText(), requestVO.getPageNo(), requestVO.getSize(), requestVO.getSortType(), 
                requestVO.getCenterX(), requestVO.getCenterY(), requestVO.getMemNo());
        
        try {
            // memNo 검증
            if (requestVO.getMemNo() == null) {
                log.warn("[REQUEST] 요청 내역 조회 실패 - memNo가 없습니다.");
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "회원 번호(memNo)가 없습니다.");
            }

            // 기본값 설정
            int pageNo = requestVO.getPageNo() != null ? requestVO.getPageNo() : 1;
            int size = requestVO.getSize() != null ? requestVO.getSize() : 10;
            String sortType = requestVO.getSortType() != null ? requestVO.getSortType() : "crtDt";
            
            // 페이지 번호를 0부터 시작하는 인덱스로 변환 (MyBatis는 0부터 시작)
            int pageNoIndex = (pageNo - 1) * size;
            
            // sortType을 컬럼명으로 변환
            String columnSortType = convertSortTypeToColumn(sortType);
            requestVO.setPageNo(pageNoIndex);
            requestVO.setSize(size);
            requestVO.setColumnSortType(columnSortType);

            List<RequestVO> requestList = requestMapper.selectRequestHistory(requestVO);
            
            // 복호화 처리 및 encryptedSeq 추가
            for (RequestVO requestVo : requestList) {
                if (requestVo.getCrtName() != null) {
                    requestVo.setCrtName(AesCryptoUtil.decrypt(requestVo.getCrtName()));
                }
                if (requestVo.getCrtPhoneNumber() != null) {
                    requestVo.setCrtPhoneNumber(AesCryptoUtil.decrypt(requestVo.getCrtPhoneNumber()));
                }
                // seq 암호화하여 응답에 포함
                if (requestVo.getSeq() != null) {
                    requestVo.setEncryptedSeq(AesCryptoUtil.encrypt(String.valueOf(requestVo.getSeq())));
                }
            }
            
            log.info("[REQUEST] 요청 내역 조회 완료 - 조회 건수: {}", requestList.size());
            return requestList;
            
        } catch (Exception e) {
            log.error("[REQUEST] 요청 내역 조회 중 오류 발생", e);
            throw new RuntimeException("요청 내역 조회 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * 업로드 디렉토리 경로 생성
     * 
     * @return 업로드 디렉토리 경로
     */
    private String getUploadDirectory() {
        String dir = basePath;
        
        if (createDateFolder) {
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            dir = dir + File.separator + dateFolder;
        }
        
        return dir;
    }
    
    /**
     * 파일 확장자 추출
     * 
     * @param fileName 파일명
     * @return 확장자 (점 포함)
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    /**
     * 정렬 타입을 컬럼명으로 변환
     * 
     * @param sortType 정렬 타입 (최신 순, 금액 높은 순, 금액 낮은 순, 거리 순)
     * @return 정렬 컬럼명 (ORDER BY 절에 사용)
     */
    private String convertSortTypeToColumn(String sortType) {
        if (sortType == null || sortType.isEmpty()) {
            return "crt_dt DESC";
        }
        
        switch (sortType) {
            case "crt_dt":              // 최신 순
                return "crt_dt DESC";
            case "pay_amt":             // 금액 높은 순
                return "CAST(pay_amt AS INTEGER) DESC";
            case "distance":            // 거리 순
                return "distance ASC"; 
            default:
                // sortType이 이미 컬럼명 형태인 경우 (예: "crtDt", "payAmt")
                if (sortType.equals("crtDt")) {
                    return "crt_dt DESC";
                } else if (sortType.equals("payAmt")) {
                    return "CAST(pay_amt AS INTEGER) DESC";
                }
                return "distance ASC";
        }
    }

    /**
     * 요청 상세 내역 조회
     * 
     * @param requestVO 요청 VO (seq 포함)
     * @return 요청 상세 정보 및 파일 목록
     */
    public Map<String, Object> getRequestHistoryDetail(RequestVO requestVO) {
        log.info("[REQUEST] 요청 상세 내역 조회 시작 - seq: {}", requestVO.getSeq());
        
        try {
            // 요청 정보 조회
            Long decryptedSeq = Long.parseLong(AesCryptoUtil.decrypt(requestVO.getEncryptedSeq()));
            RequestVO requestDetail = requestMapper.selectRequestHistoryDetail(decryptedSeq);
            
            if (requestDetail == null) {
                log.warn("[REQUEST] 요청 상세 내역이 존재하지 않습니다 - seq: {}", AesCryptoUtil.decrypt(requestVO.getEncryptedSeq()));
                throw new RuntimeException("요청 내역을 찾을 수 없습니다.");
            }
            
            // 복호화 처리
            requestDetail.setCrtName(AesCryptoUtil.decrypt(requestDetail.getCrtName()));
            requestDetail.setCrtPhoneNumber(AesCryptoUtil.decrypt(requestDetail.getCrtPhoneNumber()));
            requestDetail.setExecName(requestDetail.getExecName() != null ? AesCryptoUtil.decrypt(requestDetail.getExecName()) : null);
            requestDetail.setExecPhoneNumber(requestDetail.getExecPhoneNumber() != null ? AesCryptoUtil.decrypt(requestDetail.getExecPhoneNumber()) : null);
            requestDetail.setAccountHolder(requestDetail.getAccountHolder() != null ? AesCryptoUtil.decrypt(requestDetail.getAccountHolder()) : null);
            requestDetail.setAccountNumber(requestDetail.getAccountNumber() != null ? AesCryptoUtil.decrypt(requestDetail.getAccountNumber()) : null);
            
            // 파일 정보 조회
            List<RequestFileVO> fileList = requestMapper.selectRequestFilesBySeq(Long.parseLong(AesCryptoUtil.decrypt(requestVO.getEncryptedSeq())), requestDetail.getExecSeq());
            
            // seq 암호화하여 응답에 포함
            String encryptedSeq = AesCryptoUtil.encrypt(String.valueOf(Long.parseLong(AesCryptoUtil.decrypt(requestVO.getEncryptedSeq()))));
            requestDetail.setEncryptedSeq(encryptedSeq);
            
            // 결과 구성
            Map<String, Object> result = new HashMap<>();
            result.put("requestDetail", requestDetail);
            result.put("fileList", fileList != null ? fileList : new ArrayList<>());

            log.info("[REQUEST] 요청 상세 내역 조회 완료 - seq: {}, fileCount: {}", 
                    requestVO.getSeq(), fileList != null ? fileList.size() : 0);
            
            return result;
            
        } catch (Exception e) {
            log.error("[REQUEST] 요청 상세 내역 조회 중 오류 발생 - seq: {}", AesCryptoUtil.decrypt(requestVO.getEncryptedSeq()), e);
            throw new RuntimeException("요청 상세 내역 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 요청 취소
     * 
     * @param requestVO 요청 정보 (seq, memNo 포함)
     * @return 취소된 요청 정보
     */
    @Transactional
    public RequestVO cancelRequest(RequestVO requestVO) {
        log.info("[REQUEST] 요청 취소 시작 - seq: {}, memNo: {}", 
                requestVO.getSeq(), requestVO.getMemNo());
        
        try {
            // seq 검증
            if (requestVO.getSeq() == null) {
                log.warn("[REQUEST] 요청 취소 실패 - seq가 없습니다.");
                requestVO.setSuccess(false);
                throw new ValidationException(Status.파라미터오류, "요청 번호(seq)가 필요합니다.");
            }
            
            // memNo 검증
            if (requestVO.getMemNo() == null) {
                log.warn("[REQUEST] 요청 취소 실패 - memNo가 없습니다.");
                requestVO.setSuccess(false);
                throw new ValidationException(Status.파라미터오류, "회원 번호(memNo)가 필요합니다.");
            }
            
            // 요청 상태 확인 (status가 '0'인지 체크)
            RequestVO existingRequest = requestMapper.selectRequestHistoryDetail(requestVO.getSeq());
            if (existingRequest == null) {
                log.warn("[REQUEST] 요청 취소 실패 - 요청이 존재하지 않습니다. seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "요청이 존재하지 않습니다.");

                
            }
            
            if(requestVO.getExecuteSw() != null && requestVO.getExecuteSw().equals("1")){
                // status가 '0'이 아니면 취소 불가
                if (!"1".equals(existingRequest.getStatus())) {
                    log.warn("[REQUEST] 수행 취소 실패 - 진행중 상태가 아닙니다. seq: {}, status: {}", 
                            requestVO.getSeq(), existingRequest.getStatus());
                    requestVO.setSuccess(false);
                    throw new ValidationException(Status.데이터없음, "진행중 상태가 아닙니다.");
                }

                if (!requestVO.getMemNo().equals(existingRequest.getExecId())) {
                    log.warn("[REQUEST] 수행 취소 실패 - 권한이 없습니다. seq: {}, memNo: {}, crtId: {}", 
                            requestVO.getSeq(), requestVO.getMemNo(), existingRequest.getExecId());
                    requestVO.setSuccess(false);
                    throw new ValidationException(Status.데이터없음, "요청 취소 권한이 없습니다.");
                }
            }else{
                // status가 '0'이 아니면 취소 불가
                if (!"0".equals(existingRequest.getStatus())) {
                    log.warn("[REQUEST] 요청 취소 실패 - 요청 상태가 아닙니다. seq: {}, status: {}", 
                            requestVO.getSeq(), existingRequest.getStatus());
                    requestVO.setSuccess(false);
                    throw new ValidationException(Status.데이터없음, "요청 상태가 아닙니다.");
                }
                // 권한 확인 (요청자와 세션 사용자가 일치하는지 확인)
                if (!requestVO.getMemNo().equals(existingRequest.getCrtId())) {
                    log.warn("[REQUEST] 요청 취소 실패 - 권한이 없습니다. seq: {}, memNo: {}, crtId: {}", 
                            requestVO.getSeq(), requestVO.getMemNo(), existingRequest.getCrtId());
                    requestVO.setSuccess(false);
                    throw new ValidationException(Status.데이터없음, "요청 취소 권한이 없습니다.");
                }
            }
            
            int result = 0;
            if(requestVO.getExecuteSw() != null && requestVO.getExecuteSw().equals("1")){
                // 수행 내역 삭제 처리
                result = requestMapper.deleteExecute(requestVO);

                // 요청 상태로 변경
                requestVO.setStatus("0");
                requestMapper.updateRequestStatus(requestVO);
                if(result <= 0){
                    log.error("[REQUEST] 수행 내역 삭제 실패 - seq: {}", requestVO.getSeq());
                    requestVO.setSuccess(false);
                    throw new RuntimeException("수행 내역 삭제에 실패했습니다.");
                }
            }else{
                // 요청 삭제 처리
                result = requestMapper.cancelRequest(requestVO);
                if(result <= 0){
                    log.error("[REQUEST] 요청 삭제 실패 - seq: {}", requestVO.getSeq());
                    requestVO.setSuccess(false);
                    throw new RuntimeException("요청 삭제에 실패했습니다.");
                }
            }
            
            if (result > 0) {
                requestVO.setSuccess(true);
                log.info("[REQUEST] 요청 취소 완료 - seq: {}", requestVO.getSeq());
            } else {
                requestVO.setSuccess(false);
                log.warn("[REQUEST] 요청 취소 실패 - seq: {}, memNo: {}", 
                        requestVO.getSeq(), requestVO.getMemNo());
                throw new ValidationException(Status.데이터없음, "요청 취소에 실패했습니다.");
            }
            
            return requestVO;
            
        } catch (ValidationException e) {
            log.error("[REQUEST] 요청 취소 검증 실패 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw e;
        } catch (Exception e) {
            log.error("[REQUEST] 요청 취소 중 오류 발생 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw new RuntimeException("요청 취소 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 수행 내역 목록조회(페이징 처리)
     * 
     * @param requestVO 조회 조건 VO
     * @return 수행 내역 목록
     */
    public List<RequestVO> getExecuteHistory(RequestVO requestVO) {
        log.info("[REQUEST] 수행 내역 조회 시작 - searchText: {}, pageNo: {}, size: {}, sortType: {}, centerX: {}, centerY: {}", 
                requestVO.getSearchText(), requestVO.getPageNo(), requestVO.getSize(), requestVO.getSortType(), 
                requestVO.getCenterX(), requestVO.getCenterY());
        
        try {
            // 기본값 설정
            int pageNo = requestVO.getPageNo() != null ? requestVO.getPageNo() : 1;
            int size = requestVO.getSize() != null ? requestVO.getSize() : 10;
            String sortType = requestVO.getSortType() != null ? requestVO.getSortType() : "crtDt";
            
            // 페이지 번호를 0부터 시작하는 인덱스로 변환 (MyBatis는 0부터 시작)
            int pageNoIndex = (pageNo - 1) * size;
            
            // sortType을 컬럼명으로 변환
            String columnSortType = convertSortTypeToColumn(sortType);
            requestVO.setPageNo(pageNoIndex);
            requestVO.setSize(size);
            requestVO.setColumnSortType(columnSortType);

            List<RequestVO> executeList = requestMapper.selectExecuteHistory(requestVO);
            
            // 복호화 처리 및 encryptedSeq 추가
            for (RequestVO requestVo : executeList) {
                if (requestVo.getCrtName() != null) {
                    requestVo.setCrtName(AesCryptoUtil.decrypt(requestVo.getCrtName()));
                }
                if (requestVo.getCrtPhoneNumber() != null) {
                    requestVo.setCrtPhoneNumber(AesCryptoUtil.decrypt(requestVo.getCrtPhoneNumber()));
                }
                if (requestVo.getAccountHolder() != null) {
                    requestVo.setAccountHolder(AesCryptoUtil.decrypt(requestVo.getAccountHolder()));
                }

                // seq 암호화하여 응답에 포함
                if (requestVo.getSeq() != null) {
                    String encryptedSeq = AesCryptoUtil.encrypt(String.valueOf(requestVo.getSeq()));
                    requestVo.setEncryptedSeq(encryptedSeq);
                }
            }
            
            log.info("[REQUEST] 수행 내역 조회 완료 - 조회 건수: {}", executeList.size());
            return executeList;
            
        } catch (Exception e) {
            log.error("[REQUEST] 수행 내역 조회 중 오류 발생", e);
            throw new RuntimeException("수행 내역 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 요청 수락
     * 
     * @param requestVO 수행 내역 정보
     * @return 저장된 수행 내역 정보
     */
    @Transactional
    public RequestVO executeRequest(RequestVO requestVO) throws Exception {
        log.info("[REQUEST] 요청 수락 시작");
        
        try {
            // 암호화 처리
            if (requestVO.getExecName() != null && !requestVO.getExecName().isEmpty()) {
                requestVO.setExecName(AesCryptoUtil.encrypt(requestVO.getExecName()));
            }
            if (requestVO.getExecPhoneNumber() != null && !requestVO.getExecPhoneNumber().isEmpty()) {
                requestVO.setExecPhoneNumber(AesCryptoUtil.encrypt(requestVO.getExecPhoneNumber()));
            }
            
            // seq 검증
            if (requestVO.getSeq() == null) {
                log.warn("[REQUEST] 요청 수락 실패 - seq가 없습니다.");
                requestVO.setSuccess(false);
                throw new ValidationException(Status.파라미터오류, "요청 번호(seq)가 필요합니다.");
            }
            
            // 요청 상태 확인 (status가 '0'인지 체크)
            RequestVO existingRequest = requestMapper.selectRequestHistoryDetail(requestVO.getSeq());
            if (existingRequest == null) {
                log.warn("[REQUEST] 요청 수락 실패 - 요청이 존재하지 않습니다. seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "요청이 존재하지 않습니다.");
            }
            
            // status가 '0'이 아니면 수락 불가
            if (!"0".equals(existingRequest.getStatus())) {
                log.warn("[REQUEST] 요청 수락 실패 - 요청 중인 건이 아닙니다. seq: {}, status: {}", 
                        requestVO.getSeq(), existingRequest.getStatus());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "요청 중인 건이 아닙니다.");
            }
            
            int result;

            // 1. request 상태 변경 (status = '1'로 변경)
            requestVO.setStatus("1"); // 수락 상태
            result = requestMapper.updateRequestStatus(requestVO);
            
            if (result <= 0) {
                log.error("[REQUEST] 요청 상태 변경 실패 - seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new RuntimeException("요청 상태 변경에 실패했습니다.");
            }
            
            // 2. execute 등록
            Long execSeq = requestMapper.getRequestSeq();
            if (execSeq == null) {
                log.error("[REQUEST] 수행 번호 조회 실패");
                requestVO.setSuccess(false);
                throw new RuntimeException("수행 번호 조회에 실패했습니다.");
            }
            
            requestVO.setExecSeq(execSeq);
            // 수행 기본 정보 등록
            result = requestMapper.insertExecute(requestVO);
            
            if (result <= 0) {
                log.error("[REQUEST] 수행 내역 등록 실패 - seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new RuntimeException("수행 내역 등록에 실패했습니다.");
            }

            // 3. 요청한 사람한테 수행한다는 sms 발송 (seq로 요청자 전화번호 찾아서 발송)
            if (existingRequest.getCrtPhoneNumber() != null && !existingRequest.getCrtPhoneNumber().isEmpty()) {
                String decryptedPhoneNumber = AesCryptoUtil.decrypt(existingRequest.getCrtPhoneNumber());
                boolean smsSw = unifiedSmsService.sendGeneralSms(decryptedPhoneNumber, existingRequest.getInstallNm() + " 요청하신 내역이 수락되었습니다. [내 활동 ＞ 요청내역]에서 확인하세요.", "1566-2122", requestVO.getMemNm());
                
                if (!smsSw) {
                    log.warn("[REQUEST] SMS 발송 실패 - 요청자 전화번호: {}", decryptedPhoneNumber);
                    // SMS 발송 실패해도 요청 수락은 성공으로 처리
                } else {
                    log.info("[REQUEST] SMS 발송 성공 - 요청자 전화번호: {}", decryptedPhoneNumber);
                }
            } else {
                log.warn("[REQUEST] 요청자 전화번호가 없어 SMS 발송을 건너뜁니다. seq: {}", requestVO.getSeq());
            }
            
            if (result > 0) {
                requestVO.setSuccess(true);
                
                // 복호화 처리 (응답용)
                if (requestVO.getExecName() != null) {
                    requestVO.setExecName(AesCryptoUtil.decrypt(requestVO.getExecName()));
                }
                if (requestVO.getExecPhoneNumber() != null) {
                    requestVO.setExecPhoneNumber(AesCryptoUtil.decrypt(requestVO.getExecPhoneNumber()));
                }
            } else {
                requestVO.setSuccess(false);
                log.warn("[REQUEST] 요청 수락 실패 - seq: {}", requestVO.getSeq());
            }
            
            return requestVO;
            
        } catch (Exception e) {
            log.error("[REQUEST] 요청 수락 중 오류 발생 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw new RuntimeException("수행 내역 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 완료 증빙 등록
     * 
     * @param requestVO 완료 증빙 정보
     * @return 저장된 완료 증빙 정보
     */
    @Transactional
    public RequestVO completionRequest(RequestVO requestVO) throws Exception {
        log.info("[REQUEST] 완료 증빙 등록 시작 - seq: {}", requestVO.getSeq());
        
        try {
            // seq 검증
            if (requestVO.getSeq() == null) {
                log.warn("[REQUEST] 완료 증빙 등록 실패 - seq가 없습니다.");
                requestVO.setSuccess(false);
                throw new ValidationException(Status.파라미터오류, "요청 번호(seq)가 필요합니다.");
            }
            
            // 요청 정보 조회 (execSeq 확인을 위해)
            RequestVO existingRequest = requestMapper.selectRequestHistoryDetail(requestVO.getSeq());
            if (existingRequest == null) {
                log.warn("[REQUEST] 완료 증빙 등록 실패 - 요청이 존재하지 않습니다. seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "요청이 존재하지 않습니다.");
            }
            
            // execSeq 확인 (수행 내역이 등록되어 있어야 함)
            if (existingRequest.getExecSeq() == null) {
                log.warn("[REQUEST] 완료 증빙 등록 실패 - 수행 내역이 없습니다. seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "수행 내역이 없습니다.");
            }
            
            // 권한 확인 (수행자와 세션 사용자가 일치하는지 확인)
            if (!requestVO.getMemNo().equals(existingRequest.getExecId())) {
                log.warn("[REQUEST] 완료 증빙 등록 실패 - 권한이 없습니다. seq: {}, memNo: {}, execId: {}", 
                        requestVO.getSeq(), requestVO.getMemNo(), existingRequest.getExecId());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "완료 증빙 등록 권한이 없습니다.");
            }
            
            // execSeq 설정
            requestVO.setExecSeq(existingRequest.getExecSeq());
            
            // 암호화 처리
            if (requestVO.getAccountHolder() != null && !requestVO.getAccountHolder().isEmpty()) {
                requestVO.setAccountHolder(AesCryptoUtil.encrypt(requestVO.getAccountHolder()));
            }

            // 계좌번호
            if (requestVO.getAccountNumber() != null && !requestVO.getAccountNumber().isEmpty()) {
                requestVO.setAccountNumber(AesCryptoUtil.encrypt(requestVO.getAccountNumber()));
            }
            
            
            // 1. 수행 내역 업데이트 (완료 증빙 정보)
            int result = requestMapper.updateExecute(requestVO);
            
            if (result <= 0) {
                log.error("[REQUEST] 수행 내역 업데이트 실패 - seq: {}, execSeq: {}", requestVO.getSeq(), requestVO.getExecSeq());
                requestVO.setSuccess(false);
                throw new RuntimeException("수행 내역 업데이트에 실패했습니다.");
            }
            
            // 2. 요청 상태 변경 (status = '2'로 변경 - 완료)
            requestVO.setStatus("2"); // 완료 상태
            result = requestMapper.updateRequestStatus(requestVO);
            
            if (result <= 0) {
                log.error("[REQUEST] 요청 상태 변경 실패 - seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new RuntimeException("요청 상태 변경에 실패했습니다.");
            }
            
            // 복호화 처리 (응답용)
            if (requestVO.getAccountHolder() != null) {
                requestVO.setAccountHolder(AesCryptoUtil.decrypt(requestVO.getAccountHolder()));
            }
            requestVO.setEncryptedExecSeq(AesCryptoUtil.encrypt(String.valueOf(requestVO.getExecSeq())));

            requestVO.setSuccess(true);
            log.info("[REQUEST] 완료 증빙 등록 완료 - seq: {}, execSeq: {}", requestVO.getSeq(), requestVO.getExecSeq());
            
            // 3. 요청한 사람한테 수행 완료 했다는 sms 발송 (전화번호 복호화 후 발송)
            if (existingRequest.getCrtPhoneNumber() != null && !existingRequest.getCrtPhoneNumber().isEmpty()) {
                String decryptedPhoneNumber = AesCryptoUtil.decrypt(existingRequest.getCrtPhoneNumber());
                boolean smsSw = unifiedSmsService.sendGeneralSms(decryptedPhoneNumber, existingRequest.getInstallNm() + " 작업이 완료되었습니다. [내 활동 ＞ 요청내역]에서 작업확인을 진행해주세요.", "1566-2122", requestVO.getMemNm());
                
                if (!smsSw) {
                    log.warn("[REQUEST] SMS 발송 실패 - 요청자 전화번호: {}", decryptedPhoneNumber);
                    // SMS 발송 실패해도 요청 수락은 성공으로 처리
                } else {
                    log.info("[REQUEST] SMS 발송 성공 - 요청자 전화번호: {}", decryptedPhoneNumber);
                }
            } else {
                log.warn("[REQUEST] 요청자 전화번호가 없어 SMS 발송을 건너뜁니다. seq: {}", requestVO.getSeq());
            }

            return requestVO;
            
        } catch (ValidationException e) {
            log.error("[REQUEST] 완료 증빙 등록 중 검증 오류 발생 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw e;
        } catch (Exception e) {
            log.error("[REQUEST] 완료 증빙 등록 중 오류 발생 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw new RuntimeException("완료 증빙 등록 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 요청 완료 처리
     * 
     * @param requestVO 요청 정보
     * @return 완료 처리된 요청 정보
     */
    @Transactional
    public RequestVO doneRequest(RequestVO requestVO) throws Exception {
        log.info("[REQUEST] 요청 완료 처리 시작 - seq: {}", requestVO.getSeq());
        
        try {
            // seq 검증
            if (requestVO.getSeq() == null) {
                log.warn("[REQUEST] 요청 완료 처리 실패 - seq가 없습니다.");
                requestVO.setSuccess(false);
                throw new ValidationException(Status.파라미터오류, "요청 번호(seq)가 필요합니다.");
            }
            
            // 요청 정보 조회
            RequestVO existingRequest = requestMapper.selectRequestHistoryDetail(requestVO.getSeq());
            if (existingRequest == null) {
                log.warn("[REQUEST] 요청 완료 처리 실패 - 요청이 존재하지 않습니다. seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "요청이 존재하지 않습니다.");
            }
            
            // 권한 확인 (요청자와 세션 사용자가 일치하는지 확인)
            if (!requestVO.getMemNo().equals(existingRequest.getCrtId())) {
                log.warn("[REQUEST] 요청 완료 처리 실패 - 권한이 없습니다. seq: {}, memNo: {}, Crtid: {}", 
                        requestVO.getSeq(), requestVO.getMemNo(), existingRequest.getCrtId());
                requestVO.setSuccess(false);
                throw new ValidationException(Status.데이터없음, "요청 완료 처리 권한이 없습니다.");
            }
            
            // 요청 상태 변경 (status = '2'로 변경 - 완료)
            requestVO.setStatus("3"); // 완료 상태
            int result = requestMapper.updateRequestStatus(requestVO);
            
            if (result <= 0) {
                log.error("[REQUEST] 요청 상태 변경 실패 - seq: {}", requestVO.getSeq());
                requestVO.setSuccess(false);
                throw new RuntimeException("요청 상태 변경에 실패했습니다.");
            }
            
            // 완료 처리된 요청 정보 조회
            RequestVO doneRequest = requestMapper.selectRequestHistoryDetail(requestVO.getSeq());
            if (doneRequest != null) {
                doneRequest.setSuccess(true);
            }
            
            log.info("[REQUEST] 요청 완료 처리 완료 - seq: {}", requestVO.getSeq());

            // 3. 수행 완료한 사람한테 작업 확인 했다는 sms 발송 (수행자 전화번호로 발송)
            if (doneRequest != null && doneRequest.getExecPhoneNumber() != null && !doneRequest.getExecPhoneNumber().isEmpty()) {
                String decryptedPhoneNumber = AesCryptoUtil.decrypt(doneRequest.getExecPhoneNumber());
                boolean smsSw = unifiedSmsService.sendGeneralSms(decryptedPhoneNumber, existingRequest.getInstallNm() + " 요청자가 작업을 확인했습니다. 곧 계좌이체가 진행될 예정입니다.", "1566-2122", requestVO.getMemNm());
                
                if (!smsSw) {
                    log.warn("[REQUEST] SMS 발송 실패 - 수행자 전화번호: {}", decryptedPhoneNumber);
                    // SMS 발송 실패해도 요청 완료는 성공으로 처리
                } else {
                    log.info("[REQUEST] SMS 발송 성공 - 수행자 전화번호: {}", decryptedPhoneNumber);
                }
            } else {
                log.warn("[REQUEST] 수행자 전화번호가 없어 SMS 발송을 건너뜁니다. seq: {}", requestVO.getSeq());
            }

            // 4. 요청한 사람한테 계좌이체 하라는 sms 발송 (요청자 전화번호로 발송)
            if (doneRequest != null && doneRequest.getCrtPhoneNumber() != null && !doneRequest.getCrtPhoneNumber().isEmpty()) {
                String decryptedPhoneNumber = AesCryptoUtil.decrypt(doneRequest.getCrtPhoneNumber());
                boolean smsSw = unifiedSmsService.sendGeneralSms(decryptedPhoneNumber, existingRequest.getInstallNm() + " 작업 확인이 완료되었습니다. 완료된 건에 대해 계좌이체를 진행해주세요.", "1566-2122", requestVO.getMemNm());

                if (!smsSw) {
                    log.warn("[REQUEST] SMS 발송 실패 - 요청자 전화번호: {}", decryptedPhoneNumber);
                    // SMS 발송 실패해도 요청 완료는 성공으로 처리
                } else {
                    log.info("[REQUEST] SMS 발송 성공 - 요청자 전화번호: {}", decryptedPhoneNumber);
                }
            } else {
                log.warn("[REQUEST] 요청자 전화번호가 없어 SMS 발송을 건너뜁니다. seq: {}", requestVO.getSeq());
            }
            
            return doneRequest;
            
        } catch (ValidationException e) {
            log.error("[REQUEST] 요청 완료 처리 중 검증 오류 발생 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw e;
        } catch (Exception e) {
            log.error("[REQUEST] 요청 완료 처리 중 오류 발생 - seq: {}", requestVO.getSeq(), e);
            requestVO.setSuccess(false);
            throw new RuntimeException("요청 완료 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * DB에 파일명, 파일경로, 원본파일명으로 검증
     * 
     * @param requestFileVO 파일 메타 정보
     * @return 파일 존재 여부
     */
    @Transactional
    public RequestFileVO selectFileByFileNmFilePathOrgFileNm(RequestFileVO requestFileVO) throws Exception {
        log.info("[REQUEST] 파일 메타 조회 시작");
        try {
            List<RequestFileVO> result = requestMapper.selectFileByFileNmFilePathOrgFileNm(requestFileVO);
            if (result == null || result.size() == 0) {
                throw new FileNotFoundException("파일을 찾을 수 없습니다.");
            }
            return result.get(0);
        } catch (Exception e) {
            log.error("[REQUEST] 파일 메타 조회 중 오류 발생", e);
            throw new RuntimeException("파일 메타 조회 중 오류가 발생했습니다.", e);
        }
    }
}
