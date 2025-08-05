package com.tcg.cardmaker.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcg.cardmaker.model.TcgCard;

/**
 * TCG卡片圖片生成服務
 * 負責根據卡片數據生成對應的卡片圖片
 */
@Service
public class CardImageGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(CardImageGeneratorService.class);
    
    @Autowired
    private ImageUploadService imageUploadService;

    // 卡片尺寸常數
    private static final int CARD_WIDTH = 400;
    private static final int CARD_HEIGHT = 560;
    private static final int BORDER_WIDTH = 10;
    private static final int CORNER_RADIUS = 20;

    // 顏色映射
    private static final Map<String, Color> RARITY_COLORS = new HashMap<>();
    private static final Map<String, Color> TYPE_COLORS = new HashMap<>();

    static {
        // 稀有度顏色
        RARITY_COLORS.put("普通", new Color(169, 169, 169)); // 灰色
        RARITY_COLORS.put("稀有", new Color(30, 144, 255));   // 藍色
        RARITY_COLORS.put("史詩", new Color(138, 43, 226));   // 紫色
        RARITY_COLORS.put("傳說", new Color(255, 215, 0));    // 金色

        // 卡片類型顏色
        TYPE_COLORS.put("生物", new Color(34, 139, 34));      // 綠色
        TYPE_COLORS.put("法術", new Color(220, 20, 60));      // 紅色
        TYPE_COLORS.put("陷阱", new Color(75, 0, 130));       // 靛色
        TYPE_COLORS.put("裝備", new Color(255, 140, 0));      // 橙色
    }

    /**
     * 生成TCG卡片圖片
     * 
     * @param card 卡片數據
     * @return 圖片的byte陣列
     * @throws IOException 圖片生成異常
     */
    public byte[] generateCardImage(TcgCard card) throws IOException {
        BufferedImage image = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 設置繪圖品質
            setupGraphicsQuality(g2d);

            // 繪製卡片背景
            drawCardBackground(g2d, card);

            // 繪製卡片邊框
            drawCardBorder(g2d, card);

            // 繪製卡片內容
            drawCardContent(g2d, card);

            // 轉換為byte陣列
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            
            log.info("成功生成卡片圖片: {}", card.getName());
            return baos.toByteArray();

        } finally {
            g2d.dispose();
        }
    }

    /**
     * 設置繪圖品質
     */
    private void setupGraphicsQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * 取得支援中文的字體
     */
    private Font getChineseFont(int style, int size) {
        // 嘗試使用系統中文字體
        String[] fontNames = {
            "Microsoft JhengHei",  // 微軟正黑體 (Windows 繁體中文)
            "Microsoft YaHei",     // 微軟雅黑 (Windows 簡體中文)
            "SimHei",              // 黑體
            "STHeiti",             // 華文黑體 (Mac)
            "PingFang SC",         // 蘋方 (Mac)
            "Noto Sans CJK TC",    // Google Noto (Linux)
            "WenQuanYi Micro Hei", // 文泉驛微米黑 (Linux)
            "DejaVu Sans",         // 備用字體
            "SansSerif"            // 最後備用
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, style, size);
            // 檢查字體是否能正確顯示中文
            if (font.canDisplay('中') && font.canDisplay('文')) {
                return font;
            }
        }
        
        // 如果都不行，使用邏輯字體
        return new Font(Font.SANS_SERIF, style, size);
    }

    /**
     * 繪製卡片背景
     */
    private void drawCardBackground(Graphics2D g2d, TcgCard card) {
        // 基礎背景色
        Color backgroundColor = getBackgroundColor(card);
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

        // 漸層效果
        GradientPaint gradient = new GradientPaint(
            0, 0, backgroundColor,
            0, CARD_HEIGHT, backgroundColor.darker()
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(BORDER_WIDTH, BORDER_WIDTH, 
                         CARD_WIDTH - 2 * BORDER_WIDTH, 
                         CARD_HEIGHT - 2 * BORDER_WIDTH, 
                         CORNER_RADIUS, CORNER_RADIUS);
    }

    /**
     * 繪製卡片邊框
     */
    private void drawCardBorder(Graphics2D g2d, TcgCard card) {
        Color borderColor = getBorderColor(card);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        g2d.drawRoundRect(BORDER_WIDTH / 2, BORDER_WIDTH / 2, 
                         CARD_WIDTH - BORDER_WIDTH, 
                         CARD_HEIGHT - BORDER_WIDTH, 
                         CORNER_RADIUS, CORNER_RADIUS);
    }

    /**
     * 繪製卡片內容
     */
    private void drawCardContent(Graphics2D g2d, TcgCard card) {
        int contentX = BORDER_WIDTH + 10;
        int contentY = BORDER_WIDTH + 20;
        int contentWidth = CARD_WIDTH - 2 * (BORDER_WIDTH + 10);

        // 繪製費用
        drawCost(g2d, card, contentX, contentY);

        // 繪製卡片名稱
        contentY += 40;
        drawCardName(g2d, card, contentX, contentY, contentWidth);

        // 繪製卡片類型和稀有度
        contentY += 40;
        drawTypeAndRarity(g2d, card, contentX, contentY, contentWidth);

        // 繪製圖片區域
        contentY += 40;
        drawCardImage(g2d, card, contentX, contentY, contentWidth, 180);

        // 繪製描述文字
        contentY += 200;
        drawDescription(g2d, card, contentX, contentY, contentWidth);

        // 繪製攻擊力和防禦力
        drawAttackDefense(g2d, card, contentX, CARD_HEIGHT - 60);
    }

    /**
     * 繪製費用
     */
    private void drawCost(Graphics2D g2d, TcgCard card, int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 300, y - 15, 40, 40);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x + 300, y - 15, 40, 40);
        
        g2d.setFont(getChineseFont(Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String cost = card.getDisplayCost();
        int textX = x + 320 - fm.stringWidth(cost) / 2;
        int textY = y + 5 + fm.getAscent() / 2;
        g2d.drawString(cost, textX, textY);
    }

    /**
     * 繪製卡片名稱
     */
    private void drawCardName(Graphics2D g2d, TcgCard card, int x, int y, int width) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(getChineseFont(Font.BOLD, 20));
        
        String name = card.getName() != null ? card.getName() : "未命名卡片";
        drawCenteredText(g2d, name, x, y, width);
    }

    /**
     * 繪製類型和稀有度
     */
    private void drawTypeAndRarity(Graphics2D g2d, TcgCard card, int x, int y, int width) {
        g2d.setFont(getChineseFont(Font.PLAIN, 14));
        
        // 類型
        g2d.setColor(TYPE_COLORS.getOrDefault(card.getType(), Color.LIGHT_GRAY));
        g2d.drawString("類型: " + (card.getType() != null ? card.getType() : "未知"), x, y);
        
        // 稀有度
        g2d.setColor(RARITY_COLORS.getOrDefault(card.getRarity(), Color.GRAY));
        String rarity = "稀有度: " + (card.getRarity() != null ? card.getRarity() : "普通");
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(rarity, x + width - fm.stringWidth(rarity), y);
    }

    /**
     * 繪製卡片圖片
     */
    private void drawCardImage(Graphics2D g2d, TcgCard card, int x, int y, int width, int height) {
        try {
            BufferedImage cardImage = loadCardImage(card);
            
            if (cardImage != null) {
                // 計算縮放比例以適應指定區域
                double scaleX = (double) width / cardImage.getWidth();
                double scaleY = (double) height / cardImage.getHeight();
                double scale = Math.min(scaleX, scaleY);
                
                int scaledWidth = (int) (cardImage.getWidth() * scale);
                int scaledHeight = (int) (cardImage.getHeight() * scale);
                
                // 計算置中位置
                int imageX = x + (width - scaledWidth) / 2;
                int imageY = y + (height - scaledHeight) / 2;
                
                // 繪製圖片背景
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, y, width, height);
                
                // 繪製圖片
                g2d.drawImage(cardImage, imageX, imageY, scaledWidth, scaledHeight, null);
                
                // 繪製圖片邊框
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x, y, width, height);
                
                log.debug("成功繪製卡片圖片: {}", card.getName());
            } else {
                // 沒有圖片時顯示佔位區域
                drawPlaceholderImage(g2d, x, y, width, height);
            }
            
        } catch (Exception e) {
            log.warn("繪製卡片圖片時發生錯誤: {}, 使用佔位圖片", e.getMessage());
            drawPlaceholderImage(g2d, x, y, width, height);
        }
    }
    
    /**
     * 載入卡片圖片
     */
    private BufferedImage loadCardImage(TcgCard card) {
        String imageUrl = card.getImageUrl();
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 檢查是否為本地檔案路徑
            if (isLocalFilePath(imageUrl)) {
                return loadLocalImage(imageUrl);
            } else {
                // 嘗試從URL下載圖片
                return loadImageFromUrl(imageUrl);
            }
        } catch (Exception e) {
            log.warn("無法載入圖片 {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }
    
    /**
     * 檢查是否為本地檔案路徑
     */
    private boolean isLocalFilePath(String path) {
        // 檢查是否為相對路徑或絕對路徑
        return path.startsWith("./uploads/") || 
               path.startsWith("uploads/") ||
               path.startsWith("C:\\") ||
               path.startsWith("/") ||
               (!path.startsWith("http://") && !path.startsWith("https://"));
    }
    
    /**
     * 載入本地圖片
     */
    private BufferedImage loadLocalImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            log.warn("本地圖片檔案不存在: {}", imagePath);
            return null;
        }
        
        BufferedImage image = ImageIO.read(imageFile);
        log.debug("成功載入本地圖片: {}", imagePath);
        return image;
    }
    
    /**
     * 從URL載入圖片
     */
    private BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        
        // 設置請求頭，模擬瀏覽器請求
        connection.setRequestProperty("User-Agent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setConnectTimeout(5000); // 5秒連接超時
        connection.setReadTimeout(10000);   // 10秒讀取超時
        
        try (InputStream inputStream = connection.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            log.debug("成功從URL載入圖片: {}", imageUrl);
            return image;
        }
    }
    
    /**
     * 繪製佔位圖片
     */
    private void drawPlaceholderImage(Graphics2D g2d, int x, int y, int width, int height) {
        // 繪製圖片佔位區域
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(x, y, width, height);
        
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2d.drawRect(x, y, width, height);
        
        // 繪製圖片佔位文字
        g2d.setColor(Color.GRAY);
        g2d.setFont(getChineseFont(Font.ITALIC, 16));
        drawCenteredText(g2d, "無圖片", x, y + height / 2, width);
    }

    /**
     * 繪製描述文字
     */
    private void drawDescription(Graphics2D g2d, TcgCard card, int x, int y, int width) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(getChineseFont(Font.PLAIN, 12));
        
        String description = card.getDescription() != null ? card.getDescription() : "無描述";
        drawWrappedText(g2d, description, x, y, width, 60);
    }

    /**
     * 繪製攻擊力和防禦力
     */
    private void drawAttackDefense(Graphics2D g2d, TcgCard card, int x, int y) {
        g2d.setFont(getChineseFont(Font.BOLD, 16));
        
        // 攻擊力
        g2d.setColor(Color.RED);
        g2d.drawString("攻擊: " + card.getDisplayAttack(), x, y);
        
        // 防禦力
        g2d.setColor(Color.BLUE);
        g2d.drawString("防禦: " + card.getDisplayDefense(), x + 150, y);
    }

    /**
     * 繪製置中文字
     */
    private void drawCenteredText(Graphics2D g2d, String text, int x, int y, int width) {
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, textX, y);
    }

    /**
     * 繪製換行文字 (改進版，支援中文)
     */
    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int width, int height) {
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int currentY = y + fm.getAscent(); // 調整起始位置
        
        // 如果文字很短，直接繪製
        if (fm.stringWidth(text) <= width) {
            g2d.drawString(text, x, currentY);
            return;
        }
        
        // 對於較長的文字，逐字符檢查換行
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            String testLine = line.toString() + ch;
            
            if (fm.stringWidth(testLine) <= width) {
                line.append(ch);
            } else {
                // 繪製當前行
                if (line.length() > 0) {
                    g2d.drawString(line.toString(), x, currentY);
                    line = new StringBuilder();
                    currentY += lineHeight;
                    
                    // 檢查是否超出高度限制
                    if (currentY > y + height) {
                        return;
                    }
                }
                line.append(ch);
            }
        }
        
        // 繪製最後一行
        if (line.length() > 0 && currentY <= y + height) {
            g2d.drawString(line.toString(), x, currentY);
        }
    }

    /**
     * 取得背景顏色
     */
    private Color getBackgroundColor(TcgCard card) {
        if (card.getBackgroundStyle() != null) {
            return switch (card.getBackgroundStyle().toLowerCase()) {
                case "火" -> new Color(255, 69, 0);
                case "水" -> new Color(0, 191, 255);
                case "草" -> new Color(50, 205, 50);
                case "雷" -> new Color(255, 255, 0);
                case "暗" -> new Color(75, 0, 130);
                default -> new Color(47, 79, 79);
            };
        }
        return new Color(47, 79, 79); // 預設深灰色
    }

    /**
     * 取得邊框顏色
     */
    private Color getBorderColor(TcgCard card) {
        if (card.getBorderColor() != null) {
            try {
                return Color.decode(card.getBorderColor());
            } catch (NumberFormatException e) {
                log.warn("無法解析邊框顏色: {}", card.getBorderColor());
            }
        }
        
        // 根據稀有度決定邊框顏色
        return RARITY_COLORS.getOrDefault(card.getRarity(), new Color(192, 192, 192)); // 銀色
    }

    /**
     * 批次生成卡片圖片並儲存到指定目錄
     */
    public void generateAndSaveCards(java.util.List<TcgCard> cards, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < cards.size(); i++) {
            TcgCard card = cards.get(i);
            byte[] imageData = generateCardImage(card);
            
            String fileName = String.format("%03d_%s.png", i + 1, 
                    card.getName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_"));
            File outputFile = new File(dir, fileName);
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                fos.write(imageData);
            }
            
            log.info("已儲存卡片圖片: {}", outputFile.getAbsolutePath());
        }
    }
}