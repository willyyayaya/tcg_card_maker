#!/bin/bash

echo "========================================"
echo "    TCG卡片製作工具 啟動腳本"
echo "========================================"
echo

echo "正在檢查Java環境..."
if ! command -v java &> /dev/null; then
    echo "[錯誤] 未找到Java環境，請先安裝Java 17或更高版本"
    echo "下載地址: https://adoptium.net/"
    exit 1
fi

echo "正在檢查Maven環境..."
if ! command -v mvn &> /dev/null; then
    echo "[錯誤] 未找到Maven環境，請先安裝Maven"
    echo "下載地址: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "[信息] 環境檢查完成"
echo "[信息] 正在編譯專案..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "[錯誤] 專案編譯失敗"
    exit 1
fi

echo "[信息] 編譯完成，正在啟動應用程式..."
echo "[信息] 請稍候，應用程式啟動後請訪問: http://localhost:8080"
echo

# 嘗試自動開啟瀏覽器 (Mac)
if command -v open &> /dev/null; then
    sleep 3 && open "http://localhost:8080" &
# 嘗試自動開啟瀏覽器 (Linux)
elif command -v xdg-open &> /dev/null; then
    sleep 3 && xdg-open "http://localhost:8080" &
fi

mvn spring-boot:run 