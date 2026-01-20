package com.zinidata.domain.common.sms.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * SMS 발송 이력 Value Object
 */
@Data
public class SmsHistoryVO {
    
    private Long id;
    private String phoneNumber;
    private String messageContent;
    private String sendType;
    private String sendStatus;
    private String sendResultCode;
    private String sendResultMessage;
    private LocalDateTime requestTime;
    private LocalDateTime sendTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
