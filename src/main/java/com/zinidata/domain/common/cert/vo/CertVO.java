package com.zinidata.domain.common.cert.vo;

import lombok.Data;

@Data
public class CertVO {

    /** 프로젝트 코드 */
    private String appCode;
    
    // input
    private String prjCd;
    private String mobileNo;
    private String memNm;
    private String randomStr;
    private String tranCallback;
    private String tranMsg;
    private String ipAddr;
    private String seqNo;
    private String certNo;
    private String pathName;
    private String loginId;

    // output
    private String checkYn;
    private String socialLoginType;
}