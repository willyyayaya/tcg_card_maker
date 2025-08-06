package com.tcg.cardmaker.exception;

/**
 * TCG卡片製作器自定義異常
 * 提供統一的異常處理機制
 */
public class CardMakerException extends Exception {
    
    private final ErrorCode errorCode;
    
    public enum ErrorCode {
        FILE_PARSING_ERROR("檔案解析錯誤"),
        IMAGE_GENERATION_ERROR("圖片生成錯誤"),
        IMAGE_UPLOAD_ERROR("圖片上傳錯誤"),
        VALIDATION_ERROR("資料驗證錯誤"),
        IO_ERROR("輸入輸出錯誤"),
        UNKNOWN_ERROR("未知錯誤");
        
        private final String description;
        
        ErrorCode(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public CardMakerException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public CardMakerException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorCode.getDescription() + ": " + getMessage();
    }
}