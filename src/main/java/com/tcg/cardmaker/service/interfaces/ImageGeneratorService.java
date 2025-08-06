package com.tcg.cardmaker.service.interfaces;

import java.io.IOException;

import com.tcg.cardmaker.model.TcgCard;

/**
 * 圖片生成服務介面
 * 定義圖片生成的契約
 * 遵循介面隔離原則 (ISP) - 專注於圖片生成功能
 */
public interface ImageGeneratorService {
    
    /**
     * 生成卡片圖片
     * 
     * @param card 卡片數據
     * @return 圖片的byte陣列
     * @throws IOException 圖片生成異常
     */
    byte[] generateCardImage(TcgCard card) throws IOException;
    
    /**
     * 獲取支援的圖片格式
     * 
     * @return 支援的格式列表
     */
    String[] getSupportedFormats();
    
    /**
     * 獲取預設圖片尺寸
     * 
     * @return [寬度, 高度]
     */
    int[] getDefaultDimensions();
}