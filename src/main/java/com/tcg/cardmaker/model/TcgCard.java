package com.tcg.cardmaker.model;

/**
 * TCG卡片數據模型
 * 定義卡片的基本屬性
 */
public class TcgCard {
    
    /**
     * 卡片名稱
     */
    private String name;
    
    /**
     * 卡片類型 (例如：生物、法術、陷阱等)
     */
    private String type;
    
    /**
     * 卡片稀有度 (例如：普通、稀有、史詩、傳說)
     */
    private String rarity;
    
    /**
     * 攻擊力
     */
    private Integer attack;
    
    /**
     * 防禦力/生命值
     */
    private Integer defense;
    
    /**
     * 法力消耗/費用
     */
    private Integer cost;
    
    /**
     * 卡片描述文字
     */
    private String description;
    
    /**
     * 卡片圖片路徑或URL
     */
    private String imageUrl;
    
    /**
     * 卡片背景風格
     */
    private String backgroundStyle;
    
    /**
     * 卡片邊框顏色
     */
    private String borderColor;
    
    /**
     * 額外屬性 (JSON格式儲存)
     */
    private String extraProperties;
    
    // Getter 方法
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getRarity() {
        return rarity;
    }
    
    public Integer getAttack() {
        return attack;
    }
    
    public Integer getDefense() {
        return defense;
    }
    
    public Integer getCost() {
        return cost;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public String getBackgroundStyle() {
        return backgroundStyle;
    }
    
    public String getBorderColor() {
        return borderColor;
    }
    
    public String getExtraProperties() {
        return extraProperties;
    }
    
    // Setter 方法
    public void setName(String name) {
        this.name = name;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setRarity(String rarity) {
        this.rarity = rarity;
    }
    
    public void setAttack(Integer attack) {
        this.attack = attack;
    }
    
    public void setDefense(Integer defense) {
        this.defense = defense;
    }
    
    public void setCost(Integer cost) {
        this.cost = cost;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setBackgroundStyle(String backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
    }
    
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
    
    public void setExtraProperties(String extraProperties) {
        this.extraProperties = extraProperties;
    }

    /**
     * 檢查卡片數據是否有效
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() 
               && type != null && !type.trim().isEmpty();
    }
    
    /**
     * 取得顯示用的攻擊力
     */
    public String getDisplayAttack() {
        return attack != null ? attack.toString() : "-";
    }
    
    /**
     * 取得顯示用的防禦力
     */
    public String getDisplayDefense() {
        return defense != null ? defense.toString() : "-";
    }
    
    /**
     * 取得顯示用的費用
     */
    public String getDisplayCost() {
        return cost != null ? cost.toString() : "0";
    }
} 