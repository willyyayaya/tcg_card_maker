package com.tcg.cardmaker.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tcg.cardmaker.model.TcgCard;
import com.tcg.cardmaker.service.interfaces.ImageGeneratorService;

/**
 * TCG卡片圖片生成服務實作
 * 遵循單一職責原則 (SRP) - 專責卡片圖片生成
 * 遵循依賴反轉原則 (DIP) - 依賴抽象介面而非具體實作
 */
@Service
public class TcgCardImageGeneratorService implements ImageGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(TcgCardImageGeneratorService.class);
    


    // 卡片尺寸常數
    private static final int CARD_WIDTH = 400;
    private static final int CARD_HEIGHT = 560;
    private static final int BORDER_WIDTH = 10;
    private static final int CORNER_RADIUS = 20;
    
    // 支援的輸出格式
    private static final String[] SUPPORTED_FORMATS = {"PNG", "JPG", "JPEG"};
    
    // 顏色配置
    private final Map<String, Color> rarityColors = initRarityColors();
    private final Map<String, Color> typeColors = initTypeColors();

    @Override
    public byte[] generateCardImage(TcgCard card) throws IOException {
        log.info("開始生成卡片圖片: {}", card.getName());
        
        // 創建畫布
        BufferedImage cardImage = createCanvas();
        Graphics2D g2d = createGraphics(cardImage);
        
        try {
            // 繪製卡片各部分
            drawCardBackground(g2d, card);
            drawCardBorder(g2d, card);
            drawCardImage(g2d, card);
            drawCardContent(g2d, card);
            
            // 轉換為byte陣列
            return imageToByteArray(cardImage);
            
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public String[] getSupportedFormats() {
        return SUPPORTED_FORMATS.clone();
    }

    @Override
    public int[] getDefaultDimensions() {
        return new int[]{CARD_WIDTH, CARD_HEIGHT};
    }

    /**
     * 創建畫布
     */
    private BufferedImage createCanvas() {
        return new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * 創建圖形上下文並設置渲染提示
     */
    private Graphics2D createGraphics(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        
        // 設置高品質渲染
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        return g2d;
    }

    /**
     * 繪製卡片背景
     */
    private void drawCardBackground(Graphics2D g2d, TcgCard card) {
        // 基礎背景
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);
        
        // 根據稀有度設置背景漸層
        Color rarityColor = rarityColors.getOrDefault(card.getRarity(), Color.LIGHT_GRAY);
        g2d.setColor(rarityColor);
        g2d.fillRoundRect(BORDER_WIDTH, BORDER_WIDTH, 
                         CARD_WIDTH - 2 * BORDER_WIDTH, CARD_HEIGHT - 2 * BORDER_WIDTH,
                         CORNER_RADIUS, CORNER_RADIUS);
    }

    /**
     * 繪製卡片邊框
     */
    private void drawCardBorder(Graphics2D g2d, TcgCard card) {
        Color borderColor = getBorderColor(card);
        g2d.setColor(borderColor);
        g2d.setStroke(new java.awt.BasicStroke(BORDER_WIDTH));
        g2d.drawRoundRect(BORDER_WIDTH / 2, BORDER_WIDTH / 2,
                         CARD_WIDTH - BORDER_WIDTH, CARD_HEIGHT - BORDER_WIDTH,
                         CORNER_RADIUS, CORNER_RADIUS);
    }

    /**
     * 繪製卡片圖片
     */
    private void drawCardImage(Graphics2D g2d, TcgCard card) {
        if (card.getImageUrl() == null || card.getImageUrl().trim().isEmpty()) {
            drawPlaceholderImage(g2d);
            return;
        }

        try {
            BufferedImage cardImg = loadCardImage(card.getImageUrl());
            if (cardImg != null) {
                // 圖片顯示區域
                int imgX = 30;
                int imgY = 80;
                int imgWidth = CARD_WIDTH - 60;
                int imgHeight = 200;
                
                // 計算縮放比例，保持比例
                double scaleX = (double) imgWidth / cardImg.getWidth();
                double scaleY = (double) imgHeight / cardImg.getHeight();
                double scale = Math.min(scaleX, scaleY);
                
                int scaledWidth = (int) (cardImg.getWidth() * scale);
                int scaledHeight = (int) (cardImg.getHeight() * scale);
                
                // 居中顯示
                int centerX = imgX + (imgWidth - scaledWidth) / 2;
                int centerY = imgY + (imgHeight - scaledHeight) / 2;
                
                g2d.drawImage(cardImg, centerX, centerY, scaledWidth, scaledHeight, null);
            } else {
                drawPlaceholderImage(g2d);
            }
        } catch (Exception e) {
            log.warn("載入圖片失敗: {}", e.getMessage());
            drawPlaceholderImage(g2d);
        }
    }

    /**
     * 繪製卡片文字內容
     */
    private void drawCardContent(Graphics2D g2d, TcgCard card) {
        Font chineseFont = getChineseFont();
        
        // 卡片名稱
        drawCardName(g2d, card.getName(), chineseFont);
        
        // 卡片類型
        drawCardType(g2d, card.getType(), chineseFont);
        
        // 屬性數值
        drawCardStats(g2d, card, chineseFont);
        
        // 描述文字
        drawCardDescription(g2d, card.getDescription(), chineseFont);
    }

    /**
     * 繪製卡片名稱
     */
    private void drawCardName(Graphics2D g2d, String name, Font baseFont) {
        if (name == null) return;
        
        Font nameFont = baseFont.deriveFont(Font.BOLD, 24f);
        g2d.setFont(nameFont);
        g2d.setColor(Color.BLACK);
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (CARD_WIDTH - fm.stringWidth(name)) / 2;
        int y = 50;
        
        g2d.drawString(name, x, y);
    }

    /**
     * 繪製卡片類型
     */
    private void drawCardType(Graphics2D g2d, String type, Font baseFont) {
        if (type == null) return;
        
        Font typeFont = baseFont.deriveFont(Font.PLAIN, 18f);
        g2d.setFont(typeFont);
        
        Color typeColor = typeColors.getOrDefault(type, Color.BLACK);
        g2d.setColor(typeColor);
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = 30;
        int y = 320;
        
        g2d.drawString("類型: " + type, x, y);
    }

    /**
     * 繪製卡片屬性數值
     */
    private void drawCardStats(Graphics2D g2d, TcgCard card, Font baseFont) {
        Font statsFont = baseFont.deriveFont(Font.BOLD, 20f);
        g2d.setFont(statsFont);
        g2d.setColor(Color.RED);
        
        int y = 380;
        
        // 攻擊力
        g2d.drawString("攻擊: " + card.getAttack(), 30, y);
        
        // 防禦力
        g2d.drawString("防禦: " + card.getDefense(), 150, y);
        
        // 費用
        g2d.drawString("費用: " + card.getCost(), 270, y);
    }

    /**
     * 繪製卡片描述
     */
    private void drawCardDescription(Graphics2D g2d, String description, Font baseFont) {
        if (description == null || description.trim().isEmpty()) return;
        
        Font descFont = baseFont.deriveFont(Font.PLAIN, 14f);
        g2d.setFont(descFont);
        g2d.setColor(Color.DARK_GRAY);
        
        // 文字換行處理
        int x = 30;
        int y = 420;
        int maxWidth = CARD_WIDTH - 60;
        
        drawWrappedText(g2d, description, x, y, maxWidth);
    }

    /**
     * 繪製換行文字
     */
    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int lineHeight = fm.getHeight();
        int currentY = y;
        
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(testLine) <= maxWidth) {
                line.append(line.length() == 0 ? "" : " ").append(word);
            } else {
                if (line.length() > 0) {
                    g2d.drawString(line.toString(), x, currentY);
                    currentY += lineHeight;
                }
                line = new StringBuilder(word);
            }
        }
        
        if (line.length() > 0) {
            g2d.drawString(line.toString(), x, currentY);
        }
    }

    /**
     * 載入卡片圖片
     */
    private BufferedImage loadCardImage(String imageUrl) {
        try {
            if (isLocalFilePath(imageUrl)) {
                return loadLocalImage(imageUrl);
            } else {
                return loadImageFromUrl(imageUrl);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("載入圖片失敗: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判斷是否為本地檔案路徑
     */
    private boolean isLocalFilePath(String path) {
        return path.startsWith("./") || path.startsWith("uploads/") || path.startsWith("\\");
    }

    /**
     * 載入本地圖片
     */
    private BufferedImage loadLocalImage(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        if (!Files.exists(path)) {
            // 嘗試在uploads目錄中尋找
            path = Paths.get("uploads", "images", Paths.get(imagePath).getFileName().toString());
        }
        
        if (Files.exists(path)) {
            return ImageIO.read(path.toFile());
        }
        
        throw new IOException("本地圖片不存在: " + imagePath);
    }

    /**
     * 從URL載入圖片
     */
    private BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "TCG Card Maker 1.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        
        try {
            return ImageIO.read(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 繪製占位符圖片
     */
    private void drawPlaceholderImage(Graphics2D g2d) {
        int imgX = 30;
        int imgY = 80;
        int imgWidth = CARD_WIDTH - 60;
        int imgHeight = 200;
        
        g2d.setColor(new Color(192, 192, 192));
        g2d.fillRect(imgX, imgY, imgWidth, imgHeight);
        
        g2d.setColor(Color.GRAY);
        g2d.drawRect(imgX, imgY, imgWidth, imgHeight);
        
        g2d.setColor(Color.DARK_GRAY);
        Font font = getChineseFont().deriveFont(Font.PLAIN, 18f);
        g2d.setFont(font);
        
        String placeholder = "無圖片";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = imgX + (imgWidth - fm.stringWidth(placeholder)) / 2;
        int textY = imgY + (imgHeight + fm.getHeight()) / 2;
        
        g2d.drawString(placeholder, textX, textY);
    }

    /**
     * 獲取邊框顏色
     */
    private Color getBorderColor(TcgCard card) {
        if (card.getBorderColor() != null && !card.getBorderColor().trim().isEmpty()) {
            return parseColor(card.getBorderColor());
        }
        return typeColors.getOrDefault(card.getType(), Color.GRAY);
    }

    /**
     * 解析顏色字串
     */
    private Color parseColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "紅色", "red" -> Color.RED;
            case "綠色", "green" -> Color.GREEN;
            case "藍色", "blue" -> Color.BLUE;
            case "黃色", "yellow" -> Color.YELLOW;
            case "紫色", "purple" -> new Color(128, 0, 128);
            case "橙色", "orange" -> Color.ORANGE;
            case "黑色", "black" -> Color.BLACK;
            case "白色", "white" -> Color.WHITE;
            default -> Color.GRAY;
        };
    }

    /**
     * 獲取支援中文的字體
     */
    private Font getChineseFont() {
        String[] fontNames = {
            "Microsoft JhengHei", "Microsoft YaHei", "SimHei", 
            "NSimSun", "SimSun", "Dialog", "SansSerif"
        };
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        for (String fontName : fontNames) {
            for (String available : availableFonts) {
                if (available.equals(fontName)) {
                    return new Font(fontName, Font.PLAIN, 16);
                }
            }
        }
        
        return new Font("Dialog", Font.PLAIN, 16);
    }

    /**
     * 將圖片轉換為byte陣列
     */
    private byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * 初始化稀有度顏色映射
     */
    private static Map<String, Color> initRarityColors() {
        Map<String, Color> colors = new HashMap<>();
        colors.put("普通", new Color(169, 169, 169)); // 灰色
        colors.put("稀有", new Color(30, 144, 255));   // 藍色
        colors.put("史詩", new Color(138, 43, 226));   // 紫色
        colors.put("傳說", new Color(255, 215, 0));    // 金色
        return colors;
    }

    /**
     * 初始化類型顏色映射
     */
    private static Map<String, Color> initTypeColors() {
        Map<String, Color> colors = new HashMap<>();
        colors.put("生物", new Color(34, 139, 34));      // 綠色
        colors.put("法術", new Color(220, 20, 60));      // 紅色
        colors.put("陷阱", new Color(75, 0, 130));       // 靛色
        colors.put("裝備", new Color(255, 140, 0));      // 橙色
        return colors;
    }
}