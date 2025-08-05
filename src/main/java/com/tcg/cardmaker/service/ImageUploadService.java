package com.tcg.cardmaker.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 圖片上傳處理服務
 * 負責圖片的上傳、驗證、處理和存儲
 */
@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);
    
    // 支援的圖片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    
    // 最大檔案大小 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 圖片存儲目錄
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 處理圖片上傳
     */
    public UploadResult uploadImage(MultipartFile file) {
        try {
            // 驗證檔案
            ValidationResult validation = validateImage(file);
            if (!validation.isValid()) {
                return UploadResult.error(validation.getErrorMessage());
            }

            // 創建上傳目錄
            Path uploadPath = createUploadDirectory();
            
            // 生成唯一檔案名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
            
            // 儲存檔案
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 生成縮圖
            String thumbnailPath = generateThumbnail(filePath.toString(), extension);
            
            log.info("圖片上傳成功: {} -> {}", originalFilename, uniqueFilename);
            
            return UploadResult.success(filePath.toString(), thumbnailPath, originalFilename);
            
        } catch (IOException e) {
            log.error("圖片上傳失敗: {}", e.getMessage());
            return UploadResult.error("圖片上傳失敗: " + e.getMessage());
        }
    }

    /**
     * 驗證圖片檔案
     */
    private ValidationResult validateImage(MultipartFile file) {
        // 檢查檔案是否為空
        if (file.isEmpty()) {
            return ValidationResult.invalid("請選擇要上傳的圖片檔案");
        }

        // 檢查檔案大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.invalid("檔案大小不能超過 10MB");
        }

        // 檢查檔案副檔名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageExtension(originalFilename)) {
            return ValidationResult.invalid("不支援的圖片格式，請上傳 JPG、PNG、GIF 或 BMP 檔案");
        }

        // 檢查檔案內容類型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ValidationResult.invalid("檔案類型錯誤，請上傳有效的圖片檔案");
        }

        return ValidationResult.valid();
    }

    /**
     * 檢查檔案副檔名是否有效
     */
    private boolean isValidImageExtension(String filename) {
        String extension = getFileExtension(filename);
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 獲取檔案副檔名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 創建上傳目錄
     */
    private Path createUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir, "images");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    /**
     * 生成縮圖
     */
    private String generateThumbnail(String imagePath, String extension) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage == null) {
                return null;
            }

            // 計算縮圖尺寸 (最大 200x200)
            int thumbnailWidth = 200;
            int thumbnailHeight = 200;
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 保持長寬比
            double ratio = Math.min((double) thumbnailWidth / originalWidth, 
                                  (double) thumbnailHeight / originalHeight);
            
            int scaledWidth = (int) (originalWidth * ratio);
            int scaledHeight = (int) (originalHeight * ratio);

            // 創建縮圖
            BufferedImage thumbnail = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            
            // 設置高品質渲染
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            // 儲存縮圖
            String thumbnailPath = imagePath.replace("." + extension, "_thumb." + extension);
            ImageIO.write(thumbnail, extension.toUpperCase(), new File(thumbnailPath));
            
            return thumbnailPath;
            
        } catch (IOException e) {
            log.warn("無法生成縮圖: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 調整圖片尺寸以適應卡片
     */
    public BufferedImage resizeImageForCard(String imagePath, int targetWidth, int targetHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(imagePath));
        if (originalImage == null) {
            throw new IOException("無法讀取圖片: " + imagePath);
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 設置高品質渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }

    /**
     * 驗證結果類
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 上傳結果類
     */
    public static class UploadResult {
        private final boolean success;
        private final String filePath;
        private final String thumbnailPath;
        private final String originalFilename;
        private final String errorMessage;

        private UploadResult(boolean success, String filePath, String thumbnailPath, 
                           String originalFilename, String errorMessage) {
            this.success = success;
            this.filePath = filePath;
            this.thumbnailPath = thumbnailPath;
            this.originalFilename = originalFilename;
            this.errorMessage = errorMessage;
        }

        public static UploadResult success(String filePath, String thumbnailPath, String originalFilename) {
            return new UploadResult(true, filePath, thumbnailPath, originalFilename, null);
        }

        public static UploadResult error(String errorMessage) {
            return new UploadResult(false, null, null, null, errorMessage);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getThumbnailPath() {
            return thumbnailPath;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}