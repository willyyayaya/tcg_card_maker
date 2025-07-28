package com.tcg.cardmaker.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tcg.cardmaker.model.TcgCard;

/**
 * Excel檔案解析服務
 * 負責解析上傳的Excel檔案並轉換為TcgCard物件列表
 */
@Service
public class ExcelParserService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParserService.class);

    /**
     * 解析Excel檔案並轉換為TCG卡片列表
     * 
     * @param file 上傳的Excel檔案
     * @return TCG卡片列表
     * @throws IOException 檔案讀取異常
     */
    public List<TcgCard> parseExcelFile(MultipartFile file) throws IOException {
        List<TcgCard> cards = new ArrayList<>();
        
        // 根據檔案類型選擇適當的Workbook
        Workbook workbook;
        String fileName = file.getOriginalFilename();
        
        try {
            if (fileName != null && fileName.toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else {
                workbook = new XSSFWorkbook(file.getInputStream());
            }
        } catch (IOException | RuntimeException e) {
            // 如果無法解析為Excel，嘗試作為CSV處理
            log.warn("無法解析為Excel格式，嘗試作為CSV處理: {}", e.getMessage());
            return parseCsvFile(file);
        }
        
        try (workbook) {
            Sheet sheet = workbook.getSheetAt(0); // 讀取第一個工作表
            
            // 跳過標題行，從第二行開始讀取數據
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // 跳過標題行
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                TcgCard card = parseRowToCard(row);
                
                if (card != null && card.isValid()) {
                    cards.add(card);
                    log.info("成功解析卡片: {}", card.getName());
                } else {
                    log.warn("跳過無效的卡片數據，行號: {}", row.getRowNum() + 1);
                }
            }
        }
        
        log.info("Excel解析完成，共解析出 {} 張卡片", cards.size());
        return cards;
    }

    /**
     * 解析CSV檔案並轉換為TCG卡片列表
     */
    private List<TcgCard> parseCsvFile(MultipartFile file) throws IOException {
        List<TcgCard> cards = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            int rowIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // 跳過標題行
                    continue;
                }
                
                TcgCard card = parseCsvLineToCard(line, rowIndex);
                if (card != null && card.isValid()) {
                    cards.add(card);
                    log.info("成功解析CSV卡片: {}", card.getName());
                } else {
                    log.warn("跳過無效的CSV卡片數據，行號: {}", rowIndex + 1);
                }
                rowIndex++;
            }
        }
        
        log.info("CSV解析完成，共解析出 {} 張卡片", cards.size());
        return cards;
    }

    /**
     * 將CSV行數據轉換為TcgCard物件
     */
    private TcgCard parseCsvLineToCard(String line, int rowIndex) {
        try {
            String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // 處理含逗號的字段
            
            if (values.length < 2) {
                return null; // 至少需要名稱和類型
            }
            
            TcgCard card = new TcgCard();
            card.setName(cleanValue(values, 0));           // A欄：名稱
            card.setType(cleanValue(values, 1));           // B欄：類型
            card.setRarity(cleanValue(values, 2));         // C欄：稀有度
            card.setAttack(parseInteger(cleanValue(values, 3)));        // D欄：攻擊力
            card.setDefense(parseInteger(cleanValue(values, 4)));       // E欄：防禦力
            card.setCost(parseInteger(cleanValue(values, 5)));          // F欄：費用
            card.setDescription(cleanValue(values, 6));    // G欄：描述
            card.setImageUrl(cleanValue(values, 7));       // H欄：圖片URL
            card.setBackgroundStyle(cleanValue(values, 8)); // I欄：背景風格
            card.setBorderColor(cleanValue(values, 9));    // J欄：邊框顏色
            
            return card;
        } catch (Exception e) {
            log.error("解析CSV行數據時發生錯誤，行號: {}, 錯誤: {}", rowIndex + 1, e.getMessage());
            return null;
        }
    }

    /**
     * 清理CSV值（移除引號和空白）
     */
    private String cleanValue(String[] values, int index) {
        if (index >= values.length) {
            return null;
        }
        String value = values[index].trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }

    /**
     * 解析整數值
     */
    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(trimmedValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 將Excel行數據轉換為TcgCard物件
     * Excel欄位順序：名稱, 類型, 稀有度, 攻擊力, 防禦力, 費用, 描述, 圖片URL, 背景風格, 邊框顏色
     * 
     * @param row Excel行數據
     * @return TcgCard物件，如果數據無效則返回null
     */
    private TcgCard parseRowToCard(Row row) {
        try {
            TcgCard card = new TcgCard();
            card.setName(getStringCellValue(row, 0));           // A欄：名稱
            card.setType(getStringCellValue(row, 1));           // B欄：類型
            card.setRarity(getStringCellValue(row, 2));         // C欄：稀有度
            card.setAttack(getIntegerCellValue(row, 3));        // D欄：攻擊力
            card.setDefense(getIntegerCellValue(row, 4));       // E欄：防禦力
            card.setCost(getIntegerCellValue(row, 5));          // F欄：費用
            card.setDescription(getStringCellValue(row, 6));    // G欄：描述
            card.setImageUrl(getStringCellValue(row, 7));       // H欄：圖片URL
            card.setBackgroundStyle(getStringCellValue(row, 8)); // I欄：背景風格
            card.setBorderColor(getStringCellValue(row, 9));    // J欄：邊框顏色
            return card;
        } catch (Exception e) {
            log.error("解析行數據時發生錯誤，行號: {}, 錯誤: {}", row.getRowNum() + 1, e.getMessage());
            return null;
        }
    }

    /**
     * 取得字串類型的儲存格值
     */
    private String getStringCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * 取得整數類型的儲存格值
     */
    private Integer getIntegerCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    String trimmedValue = cell.getStringCellValue().trim();
                    yield trimmedValue.isEmpty() ? null : Integer.valueOf(trimmedValue);
                }
                default -> null;
            };
        } catch (NumberFormatException e) {
            log.warn("無法解析整數值，行: {}, 列: {}, 值: {}", 
                    row.getRowNum() + 1, columnIndex + 1, cell.toString());
            return null;
        }
    }

    /**
     * 生成Excel模板的欄位說明
     */
    public String getExcelTemplateInfo() {
        return """
                Excel檔案格式說明：
                A欄：卡片名稱 (必填)
                B欄：卡片類型 (必填，例如：生物、法術、陷阱)
                C欄：稀有度 (例如：普通、稀有、史詩、傳說)
                D欄：攻擊力 (數字)
                E欄：防禦力 (數字)
                F欄：費用 (數字)
                G欄：卡片描述
                H欄：圖片URL或路徑
                I欄：背景風格
                J欄：邊框顏色
                
                注意：第一行為標題行，數據從第二行開始
                支援 .xlsx、.xls 和 CSV 格式
                """;
    }
} 