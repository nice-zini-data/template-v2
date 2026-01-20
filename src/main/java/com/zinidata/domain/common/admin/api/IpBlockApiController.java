package com.zinidata.domain.common.admin.api;

import com.zinidata.audit.annotation.AuditLog;
import com.zinidata.audit.enums.AuditActionType;
import com.zinidata.common.dto.ApiResponse;
import com.zinidata.domain.common.admin.service.IpBlockService;
import com.zinidata.domain.common.admin.vo.IpBlockVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IP 관리 컨트롤러 (관리자용)
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/admin/ip-management")
@RequiredArgsConstructor
public class IpBlockApiController {

    private final IpBlockService ipBlockAdminService;

    /**
     * IP 관리 메인 페이지
     */
    @AuditLog(actionType = AuditActionType.PAGE_VIEW, targetResource = "page:/admin/ip-management")
    @GetMapping
    public String ipManagementPage(Model model) {
        log.info("IP 관리 페이지 접속");
        
        model.addAttribute("pageTitle", "IP 차단 관리");
        model.addAttribute("pageDescription", "시스템 IP 차단/해제 관리");
        model.addAttribute("currentPage", "admin");
        
        return "admin/ip-management";
    }

    /**
     * IP 차단 등록 API
     */
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/admin/ip/block")
    @PostMapping("/api/block")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> blockIp(@RequestBody Map<String, Object> request) {
        try {
            String ipAddress = (String) request.get("ipAddress");
            String reason = (String) request.get("reason");
            Object ttlObj = request.get("ttlHours");
            Long ttlHours = null;
            
            if (ttlObj != null) {
                if (ttlObj instanceof Number) {
                    ttlHours = ((Number) ttlObj).longValue();
                } else if (ttlObj instanceof String) {
                    try {
                        ttlHours = Long.parseLong((String) ttlObj);
                    } catch (NumberFormatException e) {
                        // 무시하고 영구 차단으로 처리
                    }
                }
            }

            ipBlockAdminService.blockIp(ipAddress, reason, ttlHours);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "IP 차단이 등록되었습니다.");
            response.put("ipAddress", ipAddress);
            response.put("blockType", ttlHours == null ? "영구차단" : "임시차단");

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("IP 차단 등록 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("IP 차단 등록에 실패했습니다."));
        }
    }

    /**
     * IP 차단 해제 API
     */
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/admin/ip/unblock")
    @PostMapping("/api/unblock")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> unblockIp(@RequestBody Map<String, Object> request) {
        try {
            String ipAddress = (String) request.get("ipAddress");
            
            ipBlockAdminService.unblockIp(ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "IP 차단이 해제되었습니다.");
            response.put("ipAddress", ipAddress);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("IP 차단 해제 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("IP 차단 해제에 실패했습니다."));
        }
    }

    /**
     * IP 일괄 차단 해제 API
     */
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/admin/ip/unblock-bulk")
    @PostMapping("/api/unblock-bulk")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> unblockBulk(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> ipAddresses = (List<String>) request.get("ipAddresses");
            Set<String> ipSet = Set.copyOf(ipAddresses);
            
            ipBlockAdminService.unblockIps(ipSet);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "선택된 IP들의 차단이 해제되었습니다.");
            response.put("count", ipSet.size());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("IP 일괄 차단 해제 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("IP 일괄 차단 해제에 실패했습니다."));
        }
    }

    /**
     * IP 차단 목록 조회 API
     */
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/admin/ip/list")
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBlockList(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String search) {
        try {
            List<IpBlockVO> blocks;
            
            if (search != null && !search.trim().isEmpty()) {
                blocks = ipBlockAdminService.searchByIpPattern(search.trim());
            } else {
                blocks = ipBlockAdminService.getAllBlocks();
            }

            // 상태별 필터링
            if (!"all".equals(status)) {
                blocks = blocks.stream()
                    .filter(block -> status.equalsIgnoreCase(block.getStatus()))
                    .toList();
            }

            // 응답 데이터 구성
            List<Map<String, Object>> blockList = blocks.stream()
                .map(this::convertToMap)
                .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("blocks", blockList);
            response.put("total", blockList.size());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("IP 차단 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("IP 차단 목록 조회에 실패했습니다."));
        }
    }

    /**
     * IP 차단 통계 조회 API
     */
    @AuditLog(actionType = AuditActionType.API_CALL, targetResource = "api:/admin/ip/statistics")
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        try {
            Map<String, Object> stats = ipBlockAdminService.getBlockStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            log.error("IP 차단 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("통계 조회에 실패했습니다."));
        }
    }

    /**
     * IpBlockVO를 Map으로 변환
     */
    private Map<String, Object> convertToMap(IpBlockVO block) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", block.getId());
        map.put("ipAddress", block.getIpAddress());
        map.put("blockReason", block.getBlockReason());
        map.put("blockType", block.getBlockType());
        map.put("status", block.getStatus());
        map.put("statusKorean", block.getStatusKorean());
        map.put("blockedBy", block.getBlockedBy());
        map.put("unblockedBy", block.getUnblockedBy());
        map.put("blockedAt", block.getBlockedAt());
        map.put("unblockedAt", block.getUnblockedAt());
        map.put("expiresAt", block.getExpiresAt());
        map.put("remainingSeconds", block.getRemainingSeconds());
        map.put("isActive", block.isActive());
        map.put("isExpired", block.isExpired());
        return map;
    }
} 