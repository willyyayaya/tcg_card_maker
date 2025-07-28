@echo off
chcp 65001 >nul
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
echo ========================================
echo    TCG卡片製作工具 啟動腳本
echo ========================================
echo.

echo 正在檢查Java環境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [錯誤] 未找到Java環境，請先安裝Java 17或更高版本
    echo 下載地址: https://adoptium.net/
    pause
    exit /b 1
)

echo 正在檢查Maven環境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [錯誤] 未找到Maven環境，請先安裝Maven
    echo 下載地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [信息] 環境檢查完成
echo [信息] 正在編譯專案...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo [錯誤] 專案編譯失敗
    pause
    exit /b 1
)

echo [信息] 編譯完成，正在啟動應用程式...
echo [信息] 請稍候，應用程式啟動後會自動開啟瀏覽器
echo [信息] 如果瀏覽器未自動開啟，請手動訪問: http://localhost:8080
echo.

start "" "http://localhost:8080"
call mvn spring-boot:run -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dspring.output.ansi.enabled=never -Djava.awt.headless=true

pause 