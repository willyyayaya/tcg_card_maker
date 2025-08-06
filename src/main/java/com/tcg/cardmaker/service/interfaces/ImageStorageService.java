package com.tcg.cardmaker.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

/**
 * 圖片存儲服務介面
 * 定義圖片存儲和處理的契約
 * 遵循介面隔離原則 (ISP) - 專注於圖片存儲功能
 */
public interface ImageStorageService {
    
    /**
     * 上傳結果封裝類別
     */
    class UploadResult {
        private final boolean success;
        private final String filePath;
        private final String thumbnailPath;
        private final String originalFilename;
        private final String errorMessage;
        
        public UploadResult(boolean success, String filePath, String thumbnailPath, 
                          String originalFilename, String errorMessage) {
            this.success = success;
            this.filePath = filePath;
            this.thumbnailPath = thumbnailPath;
            this.originalFilename = originalFilename;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getFilePath() { return filePath; }
        public String getThumbnailPath() { return thumbnailPath; }
        public String getOriginalFilename() { return originalFilename; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 上傳並存儲圖片
     * 
     * @param file 圖片檔案
     * @return 上傳結果
     */
    UploadResult uploadImage(MultipartFile file);
    
    /**
     * 驗證圖片檔案是否有效
     * 
     * @param file 要驗證的檔案
     * @return 驗證結果
     */
    boolean validateImageFile(MultipartFile file);
    
    /**
     * 獲取圖片完整路徑
     * 
     * @param filename 檔案名稱
     * @return 完整路徑
     */
    String getImagePath(String filename);
}