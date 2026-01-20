package com.zinidata.domain.common.file.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;

import lombok.extern.slf4j.Slf4j;

/**
 * 예제 페이지 컨트롤러
 * 
 * <p>개발 및 테스트를 위한 예제 페이지들을 처리합니다.</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/file")
public class FileDownloadController {

    /**
     * 파일 다운로드 예제 페이지
     * 
     * @param model 뷰 모델
     * @return 파일 다운로드 예제 페이지 뷰 이름
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/examples/file-download")
    @GetMapping("/file-download-example")
    public String fileDownloadExample(Model model) {
        log.info("파일 다운로드 예제 페이지 접속");
        
        model.addAttribute("pageTitle", "파일 다운로드 예제 - ZiniData");
        model.addAttribute("pageDescription", "보안이 적용된 5초 만료 Presigned URL 방식 파일 다운로드 시스템 예제");
        model.addAttribute("currentPage", "examples");
        
        // 예제 페이지용 추가 데이터
        model.addAttribute("exampleType", "file-download");
        model.addAttribute("featureDescription", "AWS S3 Presigned URL을 활용한 보안 파일 다운로드");
        model.addAttribute("securityFeatures", new String[]{
            "5초 만료 Presigned URL",
            "사용자 인증 필수",
            "파일 접근 권한 검증",
            "다운로드 로그 기록"
        });
        
        return "file/file-download-example";
    }
}