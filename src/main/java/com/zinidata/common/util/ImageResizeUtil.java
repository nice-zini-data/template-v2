package com.zinidata.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 이미지 리사이징 유틸리티
 * 
 * <p>이미지 파일의 크기를 줄여서 용량을 최적화합니다.</p>
 * 
 * @author ZiniData 개발팀
 * @since 1.0
 */
@Slf4j
public class ImageResizeUtil {
    
    /**
     * 최대 이미지 크기 (바이트 단위) - 기본값: 1MB
     */
    private static final long DEFAULT_MAX_IMAGE_SIZE = 1 * 1024 * 1024; // 1MB
    
    /**
     * 최대 이미지 너비 (픽셀) - 기본값: 1920px
     */
    private static final int DEFAULT_MAX_WIDTH = 1920;
    
    /**
     * 최대 이미지 높이 (픽셀) - 기본값: 1920px
     */
    private static final int DEFAULT_MAX_HEIGHT = 1920;
    
    /**
     * 이미지 품질 (0.0 ~ 1.0) - 기본값: 0.85
     */
    private static final float DEFAULT_QUALITY = 0.85f;
    
    /**
     * 이미지가 최대 크기를 넘으면 자동으로 리사이징하여 MultipartFile로 반환
     * 
     * @param file 원본 파일
     * @param maxSizeBytes 최대 크기 (바이트 단위), null이면 기본값(5MB) 사용
     * @return 리사이징된 MultipartFile (크기가 적절하면 원본 반환)
     */
    public static MultipartFile resizeImageIfNeeded(MultipartFile file, Long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            return file;
        }
        
        // 이미지 파일인지 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return file; // 이미지가 아니면 그대로 반환
        }
        
        long maxSize = maxSizeBytes != null ? maxSizeBytes : DEFAULT_MAX_IMAGE_SIZE;
        long fileSize = file.getSize();
        
        log.info("[IMAGE_RESIZE] ========== 이미지 리사이징 유틸리티 호출 ==========");
        log.info("[IMAGE_RESIZE] 파일명: {}", file.getOriginalFilename());
        log.info("[IMAGE_RESIZE] 원본 크기: {} bytes ({} MB)", fileSize, String.format("%.2f", fileSize / 1024.0 / 1024.0));
        log.info("[IMAGE_RESIZE] 최대 허용 크기: {} bytes ({} MB)", maxSize, String.format("%.2f", maxSize / 1024.0 / 1024.0));
        
        // 파일 크기가 최대 크기보다 작으면 리사이징 불필요
        if (fileSize <= maxSize) {
            log.info("[IMAGE_RESIZE] ✅ 파일 크기가 적절함 - 리사이징 불필요");
            log.info("[IMAGE_RESIZE] 현재 크기: {} bytes ({} MB) <= 최대 크기: {} bytes ({} MB)", 
                    fileSize, String.format("%.2f", fileSize / 1024.0 / 1024.0),
                    maxSize, String.format("%.2f", maxSize / 1024.0 / 1024.0));
            log.info("[IMAGE_RESIZE] ==========================================");
            return file;
        }
        
        log.info("[IMAGE_RESIZE] ⚠️ 파일 크기가 최대 크기를 초과함 - 리사이징 시작");
        log.info("[IMAGE_RESIZE] 초과량: {} bytes ({} MB)", 
                fileSize - maxSize, String.format("%.2f", (fileSize - maxSize) / 1024.0 / 1024.0));
        log.info("[IMAGE_RESIZE] ==========================================");
        
        try {
            // 이미지 읽기
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                log.warn("[IMAGE_RESIZE] 이미지를 읽을 수 없습니다 - 파일: {}", file.getOriginalFilename());
                return file; // 읽을 수 없으면 원본 반환
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            log.info("[IMAGE_RESIZE] 이미지 해상도 정보:");
            log.info("[IMAGE_RESIZE]   - 원본 해상도: {} x {} pixels", originalWidth, originalHeight);
            log.info("[IMAGE_RESIZE]   - 최대 허용 해상도: {} x {} pixels", DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
            
            // 이미지 크기 계산 (비율 유지)
            int[] newDimensions = calculateDimensions(originalWidth, originalHeight, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
            int newWidth = newDimensions[0];
            int newHeight = newDimensions[1];
            
            log.info("[IMAGE_RESIZE] 리사이징 계획:");
            log.info("[IMAGE_RESIZE]   - 목표 해상도: {} x {} pixels", newWidth, newHeight);
            if (newWidth != originalWidth || newHeight != originalHeight) {
                double widthRatio = (double) newWidth / originalWidth * 100.0;
                double heightRatio = (double) newHeight / originalHeight * 100.0;
                log.info("[IMAGE_RESIZE]   - 크기 비율: {}% x {}%", String.format("%.1f", widthRatio), String.format("%.1f", heightRatio));
            }
            
            // 리사이징
            BufferedImage resizedImage = resizeImage(originalImage, newWidth, newHeight);
            
            // 이미지 포맷 결정
            String formatName = getImageFormat(contentType);
            
            // 바이트 배열로 변환 (품질 조절)
            byte[] imageBytes = imageToBytes(resizedImage, formatName, DEFAULT_QUALITY);
            
            // 크기가 여전히 최대 크기를 넘으면 품질을 낮춰서 재시도
            int attempts = 0;
            float quality = DEFAULT_QUALITY;
            long currentSize = imageBytes.length;
            log.info("[IMAGE_RESIZE] 품질 조정 단계 시작 - 초기 크기: {} bytes ({} MB), 목표 크기: {} bytes ({} MB)", 
                    currentSize, String.format("%.2f", currentSize / 1024.0 / 1024.0),
                    maxSize, String.format("%.2f", maxSize / 1024.0 / 1024.0));
            
            while (imageBytes.length > maxSize && attempts < 5 && quality > 0.3f) {
                quality -= 0.1f;
                log.info("[IMAGE_RESIZE] 품질 조정 시도 {} - 품질: {}%, 현재 크기: {} bytes ({} MB)", 
                        attempts + 1, String.format("%.0f", quality * 100), 
                        imageBytes.length, String.format("%.2f", imageBytes.length / 1024.0 / 1024.0));
                imageBytes = imageToBytes(resizedImage, formatName, quality);
                attempts++;
                
                if (imageBytes.length <= maxSize) {
                    log.info("[IMAGE_RESIZE] ✅ 품질 조정으로 목표 달성 - 최종 품질: {}%, 최종 크기: {} bytes ({} MB)", 
                            String.format("%.0f", quality * 100), imageBytes.length, String.format("%.2f", imageBytes.length / 1024.0 / 1024.0));
                    break;
                }
            }
            
            // 크기를 더 줄여야 하면 이미지 크기를 더 줄임
            if (imageBytes.length > maxSize) {
                log.info("[IMAGE_RESIZE] 품질 조정으로 목표 미달성 - 해상도 추가 축소 시작");
                int scaleFactor = 2;
                while (imageBytes.length > maxSize && scaleFactor < 5) {
                    int scaledWidth = newWidth / scaleFactor;
                    int scaledHeight = newHeight / scaleFactor;
                    if (scaledWidth < 100 || scaledHeight < 100) {
                        log.warn("[IMAGE_RESIZE] 해상도가 너무 작아져서 중단 - 최소 크기: 100x100 pixels");
                        break; // 너무 작아지면 중단
                    }
                    log.info("[IMAGE_RESIZE] 해상도 추가 축소 시도 {} - 목표 해상도: {} x {} pixels, 현재 크기: {} bytes ({} MB)", 
                            scaleFactor - 1, scaledWidth, scaledHeight, 
                            imageBytes.length, String.format("%.2f", imageBytes.length / 1024.0 / 1024.0));
                    resizedImage = resizeImage(originalImage, scaledWidth, scaledHeight);
                    imageBytes = imageToBytes(resizedImage, formatName, 0.7f);
                    
                    if (imageBytes.length <= maxSize) {
                        log.info("[IMAGE_RESIZE] ✅ 해상도 축소로 목표 달성 - 최종 해상도: {} x {} pixels", scaledWidth, scaledHeight);
                        newWidth = scaledWidth;
                        newHeight = scaledHeight;
                        break;
                    }
                    scaleFactor++;
                }
            }
            
            long finalSize = imageBytes.length;
            long sizeReduction = fileSize - finalSize;
            double reductionPercent = (double) sizeReduction / fileSize * 100.0;
            
            log.info("[IMAGE_RESIZE] ========== 리사이징 완료 ==========");
            log.info("[IMAGE_RESIZE] 최종 결과:");
            log.info("[IMAGE_RESIZE]   - 원본 크기: {} bytes ({} MB)", fileSize, String.format("%.2f", fileSize / 1024.0 / 1024.0));
            log.info("[IMAGE_RESIZE]   - 리사이징 크기: {} bytes ({} MB)", finalSize, String.format("%.2f", finalSize / 1024.0 / 1024.0));
            log.info("[IMAGE_RESIZE]   - 크기 감소: {} bytes ({} MB, {}%)", 
                    sizeReduction, String.format("%.2f", sizeReduction / 1024.0 / 1024.0), String.format("%.2f", reductionPercent));
            log.info("[IMAGE_RESIZE]   - 최종 해상도: {} x {} pixels", newWidth, newHeight);
            log.info("[IMAGE_RESIZE]   - 이미지 포맷: {}", formatName);
            
            if (finalSize <= maxSize) {
                log.info("[IMAGE_RESIZE] ✅ 목표 달성 - 최대 크기 이하로 압축 완료");
            } else {
                log.warn("[IMAGE_RESIZE] ⚠️ 경고 - 목표 크기 미달성 ({} bytes 초과)", finalSize - maxSize);
            }
            log.info("[IMAGE_RESIZE] ==========================================");
            
            // MultipartFile로 변환
            return new ResizedMultipartFile(file, imageBytes, formatName);
            
        } catch (IOException e) {
            log.error("[IMAGE_RESIZE] ========== 리사이징 실패 ==========");
            log.error("[IMAGE_RESIZE] 파일명: {}", file.getOriginalFilename());
            log.error("[IMAGE_RESIZE] 오류 메시지: {}", e.getMessage());
            log.error("[IMAGE_RESIZE] 원본 파일 그대로 반환", e);
            log.error("[IMAGE_RESIZE] ==========================================");
            return file; // 오류 발생 시 원본 반환
        }
    }
    
    /**
     * 이미지 크기 계산 (비율 유지)
     */
    private static int[] calculateDimensions(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return new int[]{originalWidth, originalHeight};
        }
        
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return new int[]{newWidth, newHeight};
    }
    
    /**
     * 이미지 리사이징
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        
        return outputImage;
    }
    
    /**
     * BufferedImage를 바이트 배열로 변환
     */
    private static byte[] imageToBytes(BufferedImage image, String formatName, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if ("jpg".equals(formatName) || "jpeg".equals(formatName)) {
            // JPEG는 품질 조절 가능
            javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();
        } else {
            // PNG, GIF 등은 품질 조절 불가
            ImageIO.write(image, formatName, baos);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Content-Type에서 이미지 포맷 추출
     */
    private static String getImageFormat(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        
        if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return "jpg";
        } else if (contentType.contains("png")) {
            return "png";
        } else if (contentType.contains("gif")) {
            return "gif";
        } else if (contentType.contains("webp")) {
            return "webp";
        } else {
            return "jpg"; // 기본값
        }
    }
    
    /**
     * 리사이징된 MultipartFile 구현 클래스
     */
    private static class ResizedMultipartFile implements MultipartFile {
        private final MultipartFile originalFile;
        private final byte[] resizedBytes;
        private final String contentType;
        
        public ResizedMultipartFile(MultipartFile originalFile, byte[] resizedBytes, String formatName) {
            this.originalFile = originalFile;
            this.resizedBytes = resizedBytes;
            this.contentType = "image/" + formatName;
        }
        
        @Override
        @org.springframework.lang.NonNull
        public String getName() {
            String name = originalFile.getName();
            return name != null ? name : "";
        }
        
        @Override
        public String getOriginalFilename() {
            return originalFile.getOriginalFilename();
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public boolean isEmpty() {
            return resizedBytes.length == 0;
        }
        
        @Override
        public long getSize() {
            return resizedBytes.length;
        }
        
        @Override
        @org.springframework.lang.NonNull
        public byte[] getBytes() throws IOException {
            return resizedBytes.clone();
        }
        
        @Override
        @org.springframework.lang.NonNull
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(resizedBytes);
        }
        
        @Override
        public void transferTo(@org.springframework.lang.NonNull java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), resizedBytes);
        }
    }
}

