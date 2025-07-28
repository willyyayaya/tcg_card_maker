# TCG卡片製作工具 🎴

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

一個基於Spring Boot的TCG（Trading Card Game）卡片製作工具，可以透過Excel檔案批次生成專業的卡片圖片。

## 📋 目錄

- [功能特色](#-功能特色)
- [技術棧](#-技術棧)
- [系統需求](#-系統需求)
- [快速開始](#-快速開始)
- [使用說明](#-使用說明)
- [Excel格式說明](#-excel格式說明)
- [API文檔](#-api文檔)
- [專案結構](#-專案結構)
- [配置說明](#-配置說明)
- [常見問題](#-常見問題)
- [貢獻指南](#-貢獻指南)
- [授權協議](#-授權協議)

## ✨ 功能特色

### 🔥 核心功能
- **Excel批次匯入** - 支援.xlsx和.xls格式，一次處理多張卡片
- **自動圖片生成** - 根據卡片數據自動生成精美的卡片圖片
- **即時預覽** - 在下載前預覽卡片效果
- **批次下載** - 一鍵打包下載所有生成的卡片圖片
- **拖拽上傳** - 支援拖拽檔案上傳，操作更便利

### 🎨 卡片設計特色
- **多種稀有度** - 支援普通、稀有、史詩、傳說等稀有度
- **類型區分** - 生物、法術、陷阱、裝備等不同類型有不同顏色
- **背景風格** - 火、水、草、雷、暗等元素背景
- **自訂邊框** - 可設定自訂邊框顏色
- **響應式設計** - 卡片尺寸標準化，適合印刷

### 🌐 使用者介面
- **現代化UI** - 基於Bootstrap 5的響應式設計
- **繁體中文** - 完整的繁體中文介面
- **直觀操作** - 簡單易用的操作流程
- **即時回饋** - 完整的錯誤處理和狀態提示

## 🛠 技術棧

### 後端技術
- **Spring Boot 3.2.0** - 主要框架
- **Spring Web** - Web服務
- **Thymeleaf** - 模板引擎
- **Apache POI** - Excel檔案處理
- **Java Graphics2D** - 圖片生成
- **Lombok** - 程式碼簡化

### 前端技術
- **Bootstrap 5.1.3** - UI框架
- **Font Awesome 6.0** - 圖示庫
- **JavaScript ES6+** - 前端互動
- **HTML5 & CSS3** - 標準網頁技術

### 開發工具
- **Maven** - 專案管理
- **Java 17** - 程式語言
- **Spring Boot DevTools** - 開發工具

## 💻 系統需求

### 最低需求
- **Java**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **記憶體**: 最少 512MB RAM
- **硬碟**: 最少 100MB 可用空間

### 建議需求
- **Java**: 17+ (LTS版本)
- **Maven**: 3.8+
- **記憶體**: 1GB+ RAM
- **硬碟**: 1GB+ 可用空間
- **瀏覽器**: Chrome 90+, Firefox 88+, Safari 14+

## 🚀 快速開始

### 1. 克隆專案
```bash
git clone https://github.com/your-username/tcg-card-maker.git
cd tcg-card-maker
```

### 2. 編譯專案
```bash
mvn clean compile
```

### 3. 執行應用程式
```bash
mvn spring-boot:run
```

### 4. 開啟瀏覽器
訪問 [http://localhost:8080](http://localhost:8080)

### 5. 開始使用
1. 準備Excel檔案（參考格式說明）
2. 上傳Excel檔案
3. 預覽和下載生成的卡片

## 📖 使用說明

### 步驟1：準備Excel檔案
創建一個Excel檔案，包含以下欄位：
- A欄：卡片名稱（必填）
- B欄：卡片類型（必填）
- C欄：稀有度
- D欄：攻擊力
- E欄：防禦力
- F欄：費用
- G欄：卡片描述
- H欄：圖片URL
- I欄：背景風格
- J欄：邊框顏色

### 步驟2：上傳檔案
- 點擊「選擇檔案」或直接拖拽Excel檔案到上傳區域
- 系統會自動驗證檔案格式
- 點擊「開始生成卡片」

### 步驟3：查看結果
- 系統會顯示解析出的所有卡片
- 可以個別預覽每張卡片
- 可以單獨下載或批次打包下載

### 步驟4：下載卡片
- **單張下載**：點擊卡片下方的「下載」按鈕
- **批次下載**：點擊「打包下載」按鈕，會生成ZIP檔案

## 📊 Excel格式說明

### 必填欄位
| 欄位 | 說明 | 範例 |
|------|------|------|
| A欄 | 卡片名稱 | 火龍戰士 |
| B欄 | 卡片類型 | 生物 |

### 選填欄位
| 欄位 | 說明 | 可選值 | 範例 |
|------|------|--------|------|
| C欄 | 稀有度 | 普通、稀有、史詩、傳說 | 稀有 |
| D欄 | 攻擊力 | 數字 | 5 |
| E欄 | 防禦力 | 數字 | 3 |
| F欄 | 費用 | 數字 | 4 |
| G欄 | 卡片描述 | 文字 | 強大的火龍戰士 |
| H欄 | 圖片URL | URL或路徑 | https://example.com/image.jpg |
| I欄 | 背景風格 | 火、水、草、雷、暗 | 火 |
| J欄 | 邊框顏色 | 顏色代碼 | #FF0000 |

### 範例Excel內容
```
卡片名稱    | 類型 | 稀有度 | 攻擊力 | 防禦力 | 費用 | 描述
火龍戰士    | 生物 | 稀有   | 5      | 3      | 4    | 強大的火龍戰士
冰霜法術    | 法術 | 普通   | -      | -      | 2    | 造成3點冰霜傷害
神聖護盾    | 裝備 | 史詩   | -      | 5      | 3    | 提供強大的防護
```

## 🔌 API文檔

### 檔案上傳
```http
POST /upload
Content-Type: multipart/form-data

參數:
- file: Excel檔案 (.xlsx 或 .xls)
```

### 預覽卡片
```http
POST /preview-card
Content-Type: application/json

Body: TcgCard物件
Response: PNG圖片
```

### 下載單張卡片
```http
POST /generate-card
Content-Type: application/json

Body: TcgCard物件
Response: PNG圖片檔案
```

### 批次下載
```http
POST /generate-all
Content-Type: application/json

Body: TcgCard陣列
Response: ZIP檔案
```

## 📁 專案結構

```
tcg-card-maker/
├── src/
│   ├── main/
│   │   ├── java/com/tcg/cardmaker/
│   │   │   ├── TcgCardMakerApplication.java    # 主應用程式
│   │   │   ├── controller/
│   │   │   │   └── CardMakerController.java    # 控制器
│   │   │   ├── service/
│   │   │   │   ├── ExcelParserService.java     # Excel解析服務
│   │   │   │   └── CardImageGeneratorService.java # 圖片生成服務
│   │   │   └── model/
│   │   │       └── TcgCard.java                # 卡片數據模型
│   │   └── resources/
│   │       ├── application.yml                 # 應用程式配置
│   │       └── templates/
│   │           ├── index.html                  # 首頁模板
│   │           └── result.html                 # 結果頁模板
│   └── test/                                   # 測試檔案
├── logs/                                       # 日誌檔案
├── output/                                     # 輸出目錄
├── temp/                                       # 暫存目錄
├── pom.xml                                     # Maven配置
└── README.md                                   # 專案說明
```

## ⚙️ 配置說明

### application.yml 主要配置

```yaml
server:
  port: 8080                    # 服務端口

spring:
  servlet:
    multipart:
      max-file-size: 50MB       # 最大檔案大小
      max-request-size: 50MB    # 最大請求大小

tcg:
  card:
    output-dir: ./output/cards  # 輸出目錄
    max-cards-per-batch: 100    # 批次處理上限
  image:
    format: PNG                 # 圖片格式
    width: 400                  # 圖片寬度
    height: 560                 # 圖片高度
```

### 自訂配置選項

你可以透過修改 `application.yml` 來調整：
- 服務端口
- 檔案上傳限制
- 圖片尺寸和格式
- 輸出目錄路徑
- 日誌級別

## ❓ 常見問題

### Q: 支援哪些Excel格式？
A: 支援 .xlsx 和 .xls 兩種格式。

### Q: 最多可以處理多少張卡片？
A: 預設上限是100張，可以在配置檔案中調整。

### Q: 生成的圖片是什麼格式？
A: 預設生成PNG格式，尺寸為400x560像素。

### Q: 如何自訂卡片樣式？
A: 可以修改 `CardImageGeneratorService.java` 中的繪圖邏輯。

### Q: 檔案上傳失敗怎麼辦？
A: 檢查檔案大小是否超過50MB，格式是否正確。

### Q: 如何修改卡片尺寸？
A: 在 `application.yml` 中修改 `tcg.image.width` 和 `tcg.image.height`。

## 🤝 貢獻指南

我們歡迎任何形式的貢獻！

### 如何貢獻
1. Fork 這個專案
2. 創建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟一個 Pull Request

### 開發指南
- 遵循Java編碼規範
- 添加適當的註釋
- 編寫單元測試
- 更新相關文檔

### 回報問題
如果你發現了bug或有功能建議，請在 [Issues](https://github.com/your-username/tcg-card-maker/issues) 頁面提交。

## 📄 授權協議

本專案採用 MIT 授權協議 - 查看 [LICENSE](LICENSE) 檔案了解詳情。

## 🙏 致謝

- [Spring Boot](https://spring.io/projects/spring-boot) - 優秀的Java框架
- [Apache POI](https://poi.apache.org/) - Excel處理庫
- [Bootstrap](https://getbootstrap.com/) - 前端UI框架
- [Font Awesome](https://fontawesome.com/) - 圖示庫

## 📞 聯絡方式

- 專案首頁: [https://github.com/your-username/tcg-card-maker](https://github.com/your-username/tcg-card-maker)
- 問題回報: [Issues](https://github.com/your-username/tcg-card-maker/issues)
- 電子郵件: your-email@example.com

---

⭐ 如果這個專案對你有幫助，請給我們一個星星！

**快樂的卡片製作！** 🎴✨ 