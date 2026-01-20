
package com.zinidata.config;

import com.zinidata.domain.common.auth.vo.MemberVO;
import com.zinidata.domain.common.cert.vo.CertVO;
import com.zinidata.domain.common.util.CommonUtil;
import com.zinidata.domain.requests.vo.RequestVO;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 전역 요청 바디 어드바이스
 * 모든 컨트롤러에서 공통으로 사용할 수 있는 데이터 바인딩 설정을 제공합니다.
 */
@ControllerAdvice
public class GlobalRequestBodyAdvice {

    @Value("${app.code:NBZM}")
    private String appCode;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        Object target = binder.getTarget();

        // MemberVO인 경우 appCode 설정
        if (target instanceof MemberVO) {
            MemberVO memberVO = (MemberVO) target;
            if (memberVO.getAppCode() == null) {
                memberVO.setAppCode(appCode);
            }
        }

        // CertVO인 경우 appCode 설정
        if (target instanceof CertVO) {
            CertVO certVO = (CertVO) target;
            if (certVO.getAppCode() == null) {
                certVO.setAppCode(appCode);
            }
        }

        // RequestVO인 경우 세션에서 memNo 설정
        if (target instanceof RequestVO) {
            RequestVO requestVO = (RequestVO) target;
            if (requestVO.getMemNo() == null) {
                try {
                    ServletRequestAttributes attributes = 
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        Long memNo = CommonUtil.getCurrentUserMemNo(request);
                        if (memNo != null) {
                            requestVO.setMemNo(memNo);
                        }
                    }
                } catch (Exception e) {
                    // 세션 조회 실패 시 무시 (로그인하지 않은 사용자일 수 있음)
                }
            }
        }

        // 다른 VO들도 필요하면 여기에 추가
        // if (target instanceof OtherVO) {
        //     OtherVO otherVO = (OtherVO) target;
        //     if (otherVO.getAppCode() == null) {
        //         otherVO.setAppCode(appCode);
        //     }
        // }
    }
}