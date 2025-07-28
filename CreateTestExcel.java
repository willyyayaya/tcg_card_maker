import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateTestExcel {
    public static void main(String[] args) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TCG卡片");
            
            // 創建標題行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "卡片名稱", "類型", "稀有度", "攻擊力", "防禦力", "費用", 
                "描述", "圖片URL", "背景風格", "邊框顏色"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 創建數據行
            String[][] data = {
                {"火龍戰士", "生物", "傳說", "8", "6", "7", 
                 "強大的火系龍族戰士，擁有毀滅性的火焰攻擊能力。當此卡片進場時，對所有敵方生物造成2點傷害。", 
                 "", "火", "#FFD700"},
                {"水靈法師", "生物", "史詩", "4", "5", "4", 
                 "精通水系魔法的法師，能夠治療友方生物並控制戰場。每回合開始時，治療一個友方生物2點生命值。", 
                 "", "水", "#4169E1"},
                {"雷電風暴", "法術", "稀有", "", "", "3", 
                 "召喚強大的雷電風暴，對所有敵方生物造成傷害。對所有敵方生物造成3點傷害。", 
                 "", "雷", "#FFFF00"},
                {"神聖護盾", "法術", "普通", "", "", "2", 
                 "為一個友方生物提供神聖的保護，使其免受下一次傷害。目標生物獲得「免疫傷害」直到回合結束。", 
                 "", "暗", "#9400D3"},
                {"古代遺物", "裝備", "稀有", "", "", "4", 
                 "來自遠古時代的神秘裝備，能夠增強持有者的力量。裝備的生物獲得+2/+2並且具有踐踏能力。", 
                 "", "草", "#32CD32"}
            };
            
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                }
            }
            
            // 自動調整列寬
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存檔案
            try (FileOutputStream fileOut = new FileOutputStream("test_chinese_cards.xlsx")) {
                workbook.write(fileOut);
                System.out.println("成功創建 test_chinese_cards.xlsx 檔案！");
            }
            
        } catch (IOException e) {
            System.err.println("創建Excel檔案時發生錯誤: " + e.getMessage());
        }
    }
} 