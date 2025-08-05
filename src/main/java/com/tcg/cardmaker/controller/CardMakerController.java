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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tcg.cardmaker.model.TcgCard;
import com.tcg.cardmaker.service.CardImageGeneratorService;
import com.tcg.cardmaker.service.ExcelParserService;
import com.tcg.cardmaker.service.ImageUploadService;

/**
 * TCG卡片製作控制器
 * 處理檔案上傳、卡片生成和下載功能
 */
@Controller
public class CardMakerController {

    private static final Logger log = LoggerFactory.getLogger(CardMakerController.class);
    
    private final ExcelParserService excelParserService;
    private final CardImageGeneratorService cardImageGeneratorService;
    private final ImageUploadService imageUploadService;

    public CardMakerController(ExcelParserService excelParserService, 
                              CardImageGeneratorService cardImageGeneratorService,
                              ImageUploadService imageUploadService) {
        this.excelParserService = excelParserService;
        this.cardImageGeneratorService = cardImageGeneratorService;
        this.imageUploadService = imageUploadService;
    }

    /**
     * 首頁
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("templateInfo", excelParserService.getExcelTemplateInfo());
        return "index";
    }

    /**
     * 處理Excel檔案上傳和卡片生成
     */
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // 驗證檔案
            if (file.isEmpty()) {
                model.addAttribute("error", "請選擇一個Excel檔案");
                model.addAttribute("templateInfo", excelParserService.getExcelTemplateInfo());
                return "index";
            }

            if (!isExcelFile(file)) {
                model.addAttribute("error", "請上傳Excel檔案 (.xlsx 或 .xls)");
                model.addAttribute("templateInfo", excelParserService.getExcelTemplateInfo());
                return "index";
            }

            // 解析Excel檔案
            List<TcgCard> cards = excelParserService.parseExcelFile(file);
            
            if (cards.isEmpty()) {
                model.addAttribute("error", "Excel檔案中沒有找到有效的卡片數據");
                model.addAttribute("templateInfo", excelParserService.getExcelTemplateInfo());
                return "index";
            }

            // 將卡片數據傳遞到結果頁面
            model.addAttribute("cards", cards);
            model.addAttribute("cardCount", cards.size());
            model.addAttribute("success", "成功解析 " + cards.size() + " 張卡片");

            log.info("成功處理Excel檔案，解析出 {} 張卡片", cards.size());
            return "result";

        } catch (IOException e) {
            log.error("處理Excel檔案時發生錯誤", e);
            model.addAttribute("error", "處理Excel檔案時發生錯誤: " + e.getMessage());
            model.addAttribute("templateInfo", excelParserService.getExcelTemplateInfo());
            return "index";
        } catch (Exception e) {
            log.error("未預期的錯誤", e);
            model.addAttribute("error", "發生未預期的錯誤: " + e.getMessage());
            model.addAttribute("templateInfo", excelParserService.getExcelTemplateInfo());
            return "index";
        }
    }

    /**
     * 生成單張卡片圖片
     */
    @PostMapping("/generate-card")
    @ResponseBody
    public ResponseEntity<byte[]> generateSingleCard(@RequestBody TcgCard card) {
        try {
            byte[] imageData = cardImageGeneratorService.generateCardImage(card);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                    card.getName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_") + ".png");
            
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("生成卡片圖片時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批次生成所有卡片圖片並打包下載
     */
    @PostMapping("/generate-all")
    @ResponseBody
    public ResponseEntity<byte[]> generateAllCards(@RequestBody List<TcgCard> cards) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (int i = 0; i < cards.size(); i++) {
                    TcgCard card = cards.get(i);
                    byte[] imageData = cardImageGeneratorService.generateCardImage(card);
                    
                    String fileName = String.format("%03d_%s.png", i + 1, 
                            card.getName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_"));
                    
                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    zos.write(imageData);
                    zos.closeEntry();
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "tcg_cards.zip");
            
            log.info("成功生成 {} 張卡片圖片並打包", cards.size());
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("批次生成卡片圖片時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 預覽單張卡片
     */
    @PostMapping("/preview-card")
    @ResponseBody
    public ResponseEntity<byte[]> previewCard(@RequestBody TcgCard card) {
        try {
            byte[] imageData = cardImageGeneratorService.generateCardImage(card);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setCacheControl("no-cache");
            
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("預覽卡片時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 錯誤處理頁面
     */
    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("error", "發生未知錯誤，請重試");
        return "error";
    }

    /**
     * 圖片上傳端點
     */
    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            log.info("收到圖片上傳請求: {}", file.getOriginalFilename());
            
            ImageUploadService.UploadResult result = imageUploadService.uploadImage(file);
            
            if (result.isSuccess()) {
                // 返回成功結果
                return ResponseEntity.ok(new ImageUploadResponse(
                    true, 
                    result.getFilePath(), 
                    result.getThumbnailPath(),
                    result.getOriginalFilename(),
                    null
                ));
            } else {
                // 返回錯誤
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
     * 獲取圖片檔案
     */
    @GetMapping("/image/{filename}")
    public ResponseEntity<byte[]> getImage(@RequestParam String filename) {
        try {
            // 這裡可以加入圖片獲取邏輯
            // 暫時返回404，之後會實作
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("獲取圖片時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 驗證是否為Excel檔案
     */
    private boolean isExcelFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && 
               (fileName.toLowerCase().endsWith(".xlsx") || 
                fileName.toLowerCase().endsWith(".xls"));
    }

    /**
     * 圖片上傳回應類
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