import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class FontTest {
    public static void main(String[] args) {
        try {
            // 創建測試圖片
            BufferedImage image = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 設置背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 400, 200);
            
            // 設置抗鋸齒
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 測試不同字體
            String[] fontNames = {
                "Microsoft JhengHei",
                "Microsoft YaHei", 
                "SimHei",
                "SansSerif",
                "Dialog"
            };
            
            String testText = "火龍戰士 - 中文測試";
            g2d.setColor(Color.BLACK);
            
            int y = 30;
            for (String fontName : fontNames) {
                Font font = new Font(fontName, Font.BOLD, 16);
                g2d.setFont(font);
                
                String displayText = fontName + ": " + testText;
                if (font.canDisplay('中') && font.canDisplay('文')) {
                    displayText += " ✓";
                } else {
                    displayText += " ✗";
                }
                
                g2d.drawString(displayText, 10, y);
                y += 25;
            }
            
            g2d.dispose();
            
            // 儲存測試圖片
            File outputFile = new File("font_test.png");
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("字體測試圖片已儲存: " + outputFile.getAbsolutePath());
            
        } catch (java.io.IOException e) {
            System.err.println("無法儲存字體測試圖片: " + e.getMessage());
        }
    }
} 