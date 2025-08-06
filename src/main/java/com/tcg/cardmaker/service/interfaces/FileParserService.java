package com.tcg.cardmaker.service.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.tcg.cardmaker.model.TcgCard;

/**
 * 檔案解析服務介面
 * 定義解析不同格式檔案的契約
 * 遵循介面隔離原則 (ISP) - 專注於檔案解析功能
 */
public interface FileParserService {
    
    /**
     * 解析檔案並轉換為卡片列表
     * 
     * @param file 上傳的檔案
     * @return 解析後的卡片列表
     * @throws Exception 解析失敗時拋出異常
     */
    List<TcgCard> parseFile(MultipartFile file) throws Exception;
    
    /**
     * 檢查是否支援該檔案格式
     * 
     * @param file 要檢查的檔案
     * @return 是否支援
     */
    boolean isSupported(MultipartFile file);
    
    /**
     * 獲取支援的檔案格式說明
     * 
     * @return 格式說明
     */
    String getSupportedFormatsInfo();
}