# TCG卡片製作器 - 重構版

一個基於Spring Boot的TCG (Trading Card Game) 卡片圖片自動生成系統。透過上傳Excel/CSV檔案來批量生成精美的卡片圖片。

## 🏗️ 架構設計

本專案遵循SOLID設計原則，提供清晰的分層架構和高度可擴展性：

### 設計原則實踐

- **SRP (單一職責原則)**: 每個服務類別專注於單一功能
- **OCP (開放封閉原則)**: 透過介面擴展功能，對修改封閉
- **LSP (里斯科夫替換原則)**: 使用介面確保可替換性
- **ISP (介面隔離原則)**: 依賴細化的專用介面
- **DIP (依賴反轉原則)**: 依賴抽象而非具體實作

## 📁 專案結構

```
src/main/java/com/tcg/cardmaker/
├── controller/
│   └── CardMakerController.java          # 控制器層
├── service/
│   ├── interfaces/                       # 服務介面層
│   │   ├── FileParserService.java        # 檔案解析介面
│   │   ├── ImageGeneratorService.java    # 圖片生成介面
│   │   └── ImageStorageService.java      # 圖片存儲介面
│   └── impl/                            # 服務實作層
│       ├── ExcelFileParserService.java   # Excel/CSV解析實作
│       ├── TcgCardImageGeneratorService.java # 卡片圖片生成實作
│       └── LocalImageStorageService.java # 本地圖片存儲實作
├── model/
│   └── TcgCard.java                     # 卡片數據模型
├── exception/
│   └── CardMakerException.java          # 自定義異常類別
└── TcgCardMakerApplication.java         # 主應用程式類別
```

## 🎯 核心功能模組

### 1. 檔案解析模組 (`FileParserService`)
- **職責**: 解析上傳的檔案並轉換為卡片數據
- **支援格式**: Excel (.xlsx, .xls)、CSV (.csv)
- **特色**: 
  - 自動格式檢測
  - 容錯處理
  - 多種格式支援

### 2. 圖片生成模組 (`ImageGeneratorService`)
- **職責**: 根據卡片數據生成圖片
- **特色**:
  - 支援中文字體
  - 動態顏色配置
  - 圖片自動縮放
  - 高品質渲染

### 3. 圖片存儲模組 (`ImageStorageService`)
- **職責**: 處理圖片上傳、驗證和存儲
- **特色**:
  - 檔案驗證
  - 自動縮圖生成
  - 安全檔案命名

## 🚀 快速開始

### 環境需求
- Java 17+
- Maven 3.6+
- Windows/Linux/macOS

### 安裝與運行

1. **編譯專案**
```bash
mvn clean package -DskipTests
```

2. **啟動應用程式**
```bash
java -jar target/card-maker-1.0.0.jar
```

3. **訪問應用程式**
```
http://localhost:8080
```

## 📋 支援的檔案格式

### Excel/CSV 欄位規格

| 欄位 | 名稱 | 類型 | 必填 | 說明 |
|------|------|------|------|------|
| A | 名稱 | 文字 | ✅ | 卡片名稱 |
| B | 類型 | 文字 | ✅ | 生物/法術/陷阱/裝備 |
| C | 稀有度 | 文字 | ✅ | 普通/稀有/史詩/傳說 |
| D | 攻擊力 | 數字 | ✅ | 卡片攻擊數值 |
| E | 防禦力 | 數字 | ✅ | 卡片防禦數值 |
| F | 費用 | 數字 | ✅ | 卡片使用費用 |
| G | 描述 | 文字 | ❌ | 卡片描述文字 |
| H | 圖片URL | 文字 | ❌ | 本地路徑或網路URL |
| I | 背景風格 | 文字 | ❌ | 背景樣式設定 |
| J | 邊框顏色 | 文字 | ❌ | 邊框顏色設定 |

### 範例數據
```csv
火龍戰士,生物,稀有,8,6,5,強大的火龍戰士,uploads/images/dragon.jpg,火焰,紅色
冰霜法師,法術,史詩,5,3,4,操控冰霜的法師,,冰霜,藍色
```

## 🔧 API 端點

### 主要端點

| 方法 | 路徑 | 功能 | 說明 |
|------|------|------|------|
| GET | `/` | 首頁 | 顯示上傳介面 |
| POST | `/upload` | 檔案上傳 | 上傳Excel/CSV檔案 |
| POST | `/upload-image` | 圖片上傳 | 上傳卡片圖片 |
| POST | `/preview-card` | 單卡預覽 | 生成單張卡片預覽 |
| POST | `/download-all` | 批量下載 | 下載所有卡片ZIP |

### 回應格式

**成功回應**:
```json
{
  "success": true,
  "filePath": "./uploads/images/abc123.png",
  "thumbnailPath": "./uploads/images/abc123_thumb.png",
  "originalFilename": "dragon.png",
  "error": null
}
```

**錯誤回應**:
```json
{
  "success": false,
  "filePath": null,
  "thumbnailPath": null,
  "originalFilename": null,
  "error": "檔案格式不支援"
}
```

## 🎨 卡片設計規格

### 尺寸設定
- **卡片尺寸**: 400 x 560 像素
- **邊框寬度**: 10 像素
- **圓角半徑**: 20 像素
- **圖片區域**: 340 x 200 像素

### 顏色配置

**稀有度顏色**:
- 普通: 灰色 (#A9A9A9)
- 稀有: 藍色 (#1E90FF)
- 史詩: 紫色 (#8A2BE2)
- 傳說: 金色 (#FFD700)

**類型顏色**:
- 生物: 綠色 (#228B22)
- 法術: 紅色 (#DC143C)
- 陷阱: 靛色 (#4B0082)
- 裝備: 橙色 (#FF8C00)

## 🔄 擴展性設計

### 新增檔案格式支援
1. 實作 `FileParserService` 介面
2. 在Spring容器中註冊服務
3. 無需修改現有代碼

### 新增圖片生成器
1. 實作 `ImageGeneratorService` 介面
2. 自定義渲染邏輯
3. 支援不同的卡片風格

### 新增存儲方式
1. 實作 `ImageStorageService` 介面
2. 支援雲端存儲、資料庫等
3. 保持統一的介面契約

## 🧪 測試

```bash
# 運行所有測試
mvn test

# 跳過測試編譯
mvn package -DskipTests

# 生成測試報告
mvn surefire-report:report
```

## 📝 配置說明

### application.yml 設定

```yaml
app:
  upload:
    dir: uploads              # 上傳目錄
    max-size: 10485760       # 最大檔案大小 (10MB)
    allowed-types:           # 允許的圖片類型
      - image/jpeg
      - image/png
      - image/gif

server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

## 🐛 故障排除

### 常見問題

1. **中文字體顯示問題**
   - 確保系統安裝中文字體
   - 檢查字體路徑配置

2. **檔案上傳失敗**
   - 檢查檔案大小限制
   - 確認檔案格式支援

3. **圖片載入失敗**
   - 確認圖片路徑正確
   - 檢查網路連接（外部URL）

### 日誌查看

```bash
# 查看應用程式日誌
tail -f logs/application.log

# 查看特定錯誤
grep ERROR logs/application.log
```

## 🤝 貢獻指南

1. Fork 專案
2. 創建功能分支
3. 提交變更
4. 推送分支
5. 建立 Pull Request

### 代碼規範
- 遵循SOLID原則
- 完整的JavaDoc註釋
- 單元測試覆蓋
- 一致的命名規範

## 📄 授權條款

MIT License - 詳見 [LICENSE](LICENSE) 檔案

## 🔗 相關連結

- [Spring Boot 官方文檔](https://spring.io/projects/spring-boot)
- [Apache POI 使用指南](https://poi.apache.org/)
- [Maven 依賴管理](https://maven.apache.org/)

---

**快樂的卡片製作！** 🎴✨