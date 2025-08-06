package com.tcg.cardmaker.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tcg.cardmaker.model.TcgCard;
import com.tcg.cardmaker.service.interfaces.FileParserService;
import com.tcg.cardmaker.service.interfaces.ImageGeneratorService;
import com.tcg.cardmaker.service.interfaces.ImageStorageService;
import com.tcg.cardmaker.exception.CardMakerException;

/**
 * 重構後的TCG卡片製作控制器
 * 遵循SOLID原則：
 * - SRP: 專責處理HTTP請求和響應
 * - OCP: 透過介面擴展功能，對修改封閉
 * - LSP: 使用介面確保可替換性
 * - ISP: 依賴細化的介面
 * - DIP: 依賴抽象而非具體實作
 */
@Controller
public class RefactoredCardMakerController {

    private static final Logger log = LoggerFactory.getLogger(RefactoredCardMakerController.class);
    
    private final FileParserService fileParserService;
    private final ImageGeneratorService imageGeneratorService;
    private final ImageStorageService imageStorageService;

    public RefactoredCardMakerController(FileParserService fileParserService, 
                                       ImageGeneratorService imageGeneratorService,
                                       ImageStorageService imageStorageService) {
        this.fileParserService = fileParserService;
        this.imageGeneratorService = imageGeneratorService;
        this.imageStorageService = imageStorageService;
    }

    /**
     * 首頁
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("templateInfo", fileParserService.getSupportedFormatsInfo());
        return "index";
    }

    /**
     * 處理檔案上傳和卡片生成
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // 檔案驗證
            ValidationResult validation = validateFile(file);
            if (!validation.isValid()) {
                return handleValidationError(validation.getErrorMessage(), model);
            }

            // 解析檔案
            List<TcgCard> cards = fileParserService.parseFile(file);
            
            if (cards.isEmpty()) {
                return handleValidationError("檔案中沒有找到有效的卡片數據", model);
            }

            // 成功處理
            model.addAttribute("cards", cards);
            model.addAttribute("cardCount", cards.size());
            model.addAttribute("success", "成功解析 " + cards.size() + " 張卡片");

            log.info("成功處理檔案: {}，解析出 {} 張卡片", file.getOriginalFilename(), cards.size());
            return "result";

        } catch (Exception e) {
            log.error("處理檔案時發生錯誤: {}", file.getOriginalFilename(), e);
            return handleProcessingError("檔案處理失敗: " + e.getMessage(), model);
        }
    }

    /**
     * 圖片上傳端點
     */
    @PostMapping("/upload-image")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            log.info("收到圖片上傳請求: {}", file.getOriginalFilename());
            
            ImageStorageService.UploadResult result = imageStorageService.uploadImage(file);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(new ImageUploadResponse(
                    true, 
                    result.getFilePath(), 
                    result.getThumbnailPath(),
                    result.getOriginalFilename(),
                    null
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(new ImageUploadResponse(false, null, null, null, result.getErrorMessage()));
            }
            
        } catch (Exception e) {
            log.error("圖片上傳時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ImageUploadResponse(false, null, null, null, "伺服器錯誤: " + e.getMessage()));
        }
    }

    /**
     * 預覽單張卡片
     */
    @PostMapping("/preview-card")
    @ResponseBody
    public ResponseEntity<byte[]> previewCard(@RequestBody TcgCard card) {
        try {
            log.info("生成卡片預覽: {}", card.getName());
            
            byte[] imageBytes = imageGeneratorService.generateCardImage(card);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);
            headers.set("Content-Disposition", "inline; filename=\"" + 
                       sanitizeFilename(card.getName()) + ".png\"");
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("生成卡片預覽失敗: {}", card.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量下載所有卡片
     */
    @PostMapping("/download-all")
    @ResponseBody
    public ResponseEntity<byte[]> downloadAllCards(@RequestBody List<TcgCard> cards) {
        try {
            log.info("批量生成 {} 張卡片", cards.size());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                
                for (int i = 0; i < cards.size(); i++) {
                    TcgCard card = cards.get(i);
                    try {
                        byte[] imageBytes = imageGeneratorService.generateCardImage(card);
                        String filename = String.format("%03d_%s.png", i + 1, sanitizeFilename(card.getName()));
                        
                        ZipEntry entry = new ZipEntry(filename);
                        zos.putNextEntry(entry);
                        zos.write(imageBytes);
                        zos.closeEntry();
                        
                    } catch (Exception e) {
                        log.warn("生成卡片失敗，跳過: {}", card.getName(), e);
                    }
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Content-Disposition", "attachment; filename=\"tcg_cards.zip\"");
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("批量下載失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== 私有輔助方法 ==========

    /**
     * 檔案驗證
     */
    private ValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.error("請選擇一個檔案");
        }

        if (!fileParserService.isSupported(file)) {
            return ValidationResult.error("不支援的檔案格式，請參考格式說明");
        }

        return ValidationResult.success();
    }

    /**
     * 處理驗證錯誤
     */
    private String handleValidationError(String errorMessage, Model model) {
        model.addAttribute("error", errorMessage);
        model.addAttribute("templateInfo", fileParserService.getSupportedFormatsInfo());
        return "index";
    }

    /**
     * 處理處理錯誤
     */
    private String handleProcessingError(String errorMessage, Model model) {
        model.addAttribute("error", errorMessage);
        model.addAttribute("templateInfo", fileParserService.getSupportedFormatsInfo());
        return "error";
    }

    /**
     * 清理檔案名稱，移除不安全字符
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fff_-]", "_");
    }

    // ========== 內部類別 ==========

    /**
     * 驗證結果封裝
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * 圖片上傳響應
     */
    public static class ImageUploadResponse {
        private final boolean success;
        private final String filePath;
        private final String thumbnailPath;
        private final String originalFilename;
        private final String error;

        public ImageUploadResponse(boolean success, String filePath, String thumbnailPath, 
                                 String originalFilename, String error) {
            this.success = success;
            this.filePath = filePath;
            this.thumbnailPath = thumbnailPath;
            this.originalFilename = originalFilename;
            this.error = error;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getFilePath() { return filePath; }
        public String getThumbnailPath() { return thumbnailPath; }
        public String getOriginalFilename() { return originalFilename; }
        public String getError() { return error; }
    }
}