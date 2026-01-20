package com.zinidata.security.ratelimit.exception;

/**
 * Rate Limit 초과 예외
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final String limitType;
    private final String identifier;
    private final int currentCount;
    private final int maxLimit;
    private final long resetTimeMillis;
    
    public RateLimitExceededException(String limitType, String identifier, 
                                     int currentCount, int maxLimit, long resetTimeMillis) {
        super(String.format("%s Rate Limit 초과 - %s: %d/%d, 재설정 시간: %d", 
              limitType, identifier, currentCount, maxLimit, resetTimeMillis));
        this.limitType = limitType;
        this.identifier = identifier;
        this.currentCount = currentCount;
        this.maxLimit = maxLimit;
        this.resetTimeMillis = resetTimeMillis;
    }
    
    public RateLimitExceededException(String limitType, String identifier, int maxLimit) {
        this(limitType, identifier, maxLimit + 1, maxLimit, System.currentTimeMillis() + 3600000);
    }
    
    public String getLimitType() {
        return limitType;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public int getMaxLimit() {
        return maxLimit;
    }
    
    public long getResetTimeMillis() {
        return resetTimeMillis;
    }
} 