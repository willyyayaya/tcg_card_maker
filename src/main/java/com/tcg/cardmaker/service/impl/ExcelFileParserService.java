package com.tcg.cardmaker.service.impl;

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
import com.tcg.cardmaker.service.interfaces.FileParserService;

/**
 * Excel檔案解析服務實作
 * 遵循單一職責原則 (SRP) - 專責Excel和CSV檔案解析
 * 遵循開放封閉原則 (OCP) - 透過介面擴展，對修改封閉
 */
@Service
public class ExcelFileParserService implements FileParserService {

    private static final Logger log = LoggerFactory.getLogger(ExcelFileParserService.class);
    
    private static final String[] SUPPORTED_EXTENSIONS = {"xlsx", "xls", "csv"};

    @Override
    public List<TcgCard> parseFile(MultipartFile file) throws Exception {
        if (!isSupported(file)) {
            throw new IllegalArgumentException("不支援的檔案格式: " + file.getOriginalFilename());
        }

        try {
            return parseExcelFile(file);
        } catch (IOException | RuntimeException e) {
            log.warn("Excel解析失敗，嘗試CSV解析: {}", e.getMessage());
            return parseCsvFile(file);
        }
    }

    @Override
    public boolean isSupported(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) return false;
        String fileName = originalFilename.toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (fileName.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSupportedFormatsInfo() {
        return """
        支援的檔案格式：Excel (.xlsx, .xls) 和 CSV (.csv)
        
        Excel/CSV 欄位順序：
        A欄：名稱 (必填)
        B欄：類型 (生物/法術/陷阱/裝備)
        C欄：稀有度 (普通/稀有/史詩/傳說)
        D欄：攻擊力 (數字)
        E欄：防禦力 (數字)
        F欄：費用 (數字)
        G欄：描述 (選填)
        H欄：圖片URL (選填)
        I欄：背景風格 (選填)
        J欄：邊框顏色 (選填)
        
        範例：
        火龍戰士,生物,稀有,8,6,5,強大的火龍戰士,https://example.com/dragon.jpg,火焰,紅色
        """;
    }

    /**
     * 解析Excel檔案
     */
    private List<TcgCard> parseExcelFile(MultipartFile file) throws IOException {
        List<TcgCard> cards = new ArrayList<>();
        
        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // 跳過標題行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                TcgCard card = parseRowToCard(row);
                if (card != null) {
                    cards.add(card);
                    log.info("解析卡片數據: {}", card.getName());
                }
            }
        }
        
        log.info("Excel解析完成，解析到 {} 張卡片", cards.size());
        return cards;
    }

    /**
     * 解析CSV檔案
     */
    private List<TcgCard> parseCsvFile(MultipartFile file) throws IOException {
        List<TcgCard> cards = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳過標題行
                }
                
                TcgCard card = parseCsvLineToCard(line);
                if (card != null) {
                    cards.add(card);
                    log.info("解析卡片數據: {}", card.getName());
                }
            }
        }
        
        log.info("CSV解析完成，解析到 {} 張卡片", cards.size());
        return cards;
    }

    /**
     * 創建適當的Workbook實例
     */
    private Workbook createWorkbook(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(file.getInputStream());
        } else {
            return new XSSFWorkbook(file.getInputStream());
        }
    }

    /**
     * 將Excel行轉換為TcgCard物件
     */
    private TcgCard parseRowToCard(Row row) {
        try {
            TcgCard card = new TcgCard();
            
            // 解析各欄位
            card.setName(getStringCellValue(row.getCell(0)));
            card.setType(getStringCellValue(row.getCell(1)));
            card.setRarity(getStringCellValue(row.getCell(2)));
            card.setAttack(getIntegerCellValue(row.getCell(3)));
            card.setDefense(getIntegerCellValue(row.getCell(4)));
            card.setCost(getIntegerCellValue(row.getCell(5)));
            card.setDescription(getStringCellValue(row.getCell(6)));
            card.setImageUrl(getStringCellValue(row.getCell(7)));
            card.setBackgroundStyle(getStringCellValue(row.getCell(8)));
            card.setBorderColor(getStringCellValue(row.getCell(9)));
            
            return isValidCard(card) ? card : null;
            
        } catch (Exception e) {
            log.warn("解析行數據失敗: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 將CSV行轉換為TcgCard物件
     */
    private TcgCard parseCsvLineToCard(String line) {
        try {
            String[] fields = line.split(",", -1); // -1 保留空欄位
            
            if (fields.length < 6) {
                log.warn("CSV行欄位不足: {}", line);
                return null;
            }
            
            TcgCard card = new TcgCard();
            card.setName(fields[0].trim());
            card.setType(fields[1].trim());
            card.setRarity(fields[2].trim());
            card.setAttack(parseInteger(fields[3].trim()));
            card.setDefense(parseInteger(fields[4].trim()));
            card.setCost(parseInteger(fields[5].trim()));
            
            // 選填欄位
            if (fields.length > 6) card.setDescription(fields[6].trim());
            if (fields.length > 7) card.setImageUrl(fields[7].trim());
            if (fields.length > 8) card.setBackgroundStyle(fields[8].trim());
            if (fields.length > 9) card.setBorderColor(fields[9].trim());
            
            return isValidCard(card) ? card : null;
            
        } catch (Exception e) {
            log.warn("解析CSV行失敗: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 驗證卡片數據完整性
     */
    private boolean isValidCard(TcgCard card) {
        return card.getName() != null && !card.getName().trim().isEmpty() &&
               card.getType() != null && !card.getType().trim().isEmpty();
    }

    /**
     * 獲取Cell的字串值
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * 獲取Cell的整數值
     */
    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) return 0;
        
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> parseInteger(cell.getStringCellValue().trim());
            default -> 0;
        };
    }

    /**
     * 解析字串為整數
     */
    private Integer parseInteger(String value) {
        try {
            return value.isEmpty() ? 0 : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("數字格式錯誤: {}", value);
            return 0;
        }
    }
}