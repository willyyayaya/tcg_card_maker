package com.tcg.cardmaker.service.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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

import com.tcg.cardmaker.service.interfaces.ImageStorageService;

/**
 * 本地檔案系統圖片存儲服務實作
 * 遵循單一職責原則 (SRP) - 專責圖片存儲和處理
 * 遵循開放封閉原則 (OCP) - 可透過介面擴展其他存儲方式
 */
@Service
public class LocalImageStorageService implements ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalImageStorageService.class);
    
    // 支援的圖片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    
    // 最大檔案大小 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 縮圖尺寸
    private static final int THUMBNAIL_SIZE = 150;
    
    // 圖片存儲目錄
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public UploadResult uploadImage(MultipartFile file) {
        try {
            // 驗證檔案
            if (!validateImageFile(file)) {
                return new UploadResult(false, null, null, null, "檔案驗證失敗");
            }
            
            // 確保目錄存在
            ensureDirectoryExists();
            
            // 生成唯一檔名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
            
            // 儲存原始檔案
            Path imagePath = saveOriginalImage(file, uniqueFilename);
            
            // 生成縮圖
            Path thumbnailPath = generateThumbnail(imagePath, uniqueFilename);
            
            log.info("圖片上傳成功: {} -> {}", originalFilename, uniqueFilename);
            
            return new UploadResult(
                true,
                imagePath.toString(),
                thumbnailPath.toString(),
                originalFilename,
                null
            );
            
        } catch (IOException | RuntimeException e) {
            log.error("圖片上傳失敗", e);
            return new UploadResult(false, null, null, null, "上傳失敗: " + e.getMessage());
        }
    }

    @Override
    public boolean validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("檔案為空");
            return false;
        }
        
        // 檢查檔案大小
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("檔案過大: {} bytes", file.getSize());
            return false;
        }
        
        // 檢查檔案格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.warn("檔案名稱為空");
            return false;
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("不支援的檔案格式: {}", extension);
            return false;
        }
        
        // 檢查MIME類型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("錯誤的MIME類型: {}", contentType);
            return false;
        }
        
        return true;
    }

    @Override
    public String getImagePath(String filename) {
        return Paths.get(uploadDir, "images", filename).toString();
    }

    /**
     * 確保目錄存在
     */
    private void ensureDirectoryExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir, "images");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("創建上傳目錄: {}", uploadPath);
        }
    }

    /**
     * 儲存原始圖片
     */
    private Path saveOriginalImage(MultipartFile file, String filename) throws IOException {
        Path imagePath = Paths.get(uploadDir, "images", filename);
        Files.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        return imagePath;
    }

    /**
     * 生成縮圖
     */
    private Path generateThumbnail(Path originalPath, String filename) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IOException("無法讀取圖片: " + originalPath);
        }
        
        // 計算縮圖尺寸
        int[] thumbnailDimensions = calculateThumbnailSize(
            originalImage.getWidth(), 
            originalImage.getHeight()
        );
        
        // 創建縮圖
        BufferedImage thumbnail = new BufferedImage(
            thumbnailDimensions[0], 
            thumbnailDimensions[1], 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = thumbnail.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImage, 0, 0, thumbnailDimensions[0], thumbnailDimensions[1], null);
        } finally {
            g2d.dispose();
        }
        
        // 儲存縮圖
        String thumbnailFilename = getFilenameWithoutExtension(filename) + "_thumb." + getFileExtension(filename);
        Path thumbnailPath = Paths.get(uploadDir, "images", thumbnailFilename);
        ImageIO.write(thumbnail, getFileExtension(filename), thumbnailPath.toFile());
        
        return thumbnailPath;
    }

    /**
     * 計算縮圖尺寸，保持比例
     */
    private int[] calculateThumbnailSize(int originalWidth, int originalHeight) {
        double ratio = Math.min(
            (double) THUMBNAIL_SIZE / originalWidth,
            (double) THUMBNAIL_SIZE / originalHeight
        );
        
        return new int[]{
            (int) (originalWidth * ratio),
            (int) (originalHeight * ratio)
        };
    }

    /**
     * 獲取檔案副檔名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    /**
     * 獲取不含副檔名的檔名
     */
    private String getFilenameWithoutExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? filename : filename.substring(0, lastDotIndex);
    }
}