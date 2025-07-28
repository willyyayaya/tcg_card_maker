@echo off
rem 設定控制台為UTF-8編碼
chcp 65001 >nul 2>&1

rem 設定環境變數
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=TW -Duser.timezone=Asia/Taipei
set MAVEN_OPTS=-Dfile.encoding=UTF-8

echo ========================================
echo    TCG卡片製作工具 (UTF-8版本)
echo ========================================
echo.

echo [信息] 正在設定UTF-8環境...
echo [信息] 當前編碼頁: 65001 (UTF-8)
echo.

echo [信息] 正在檢查Java環境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [錯誤] 未找到Java環境，請先安裝Java 17或更高版本
    pause
    exit /b 1
)

echo [信息] 正在檢查Maven環境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [錯誤] 未找到Maven環境，請先安裝Maven
    pause
    exit /b 1
)

echo [信息] 正在清理並編譯專案...
call mvn clean compile
if %errorlevel% neq 0 (
    echo [錯誤] 專案編譯失敗
    pause
    exit /b 1
)
echo [信息] 編譯成功！

echo [信息] 編譯完成，正在啟動應用程式...
echo [信息] 應用程式將在 http://localhost:8080 啟動
echo [信息] 請稍候...
echo.

rem 自動開啟瀏覽器
start "" "http://localhost:8080"

rem 啟動應用程式，使用UTF-8編碼
call mvn spring-boot:run ^
    -Dfile.encoding=UTF-8 ^
    -Duser.language=zh ^
    -Duser.country=TW ^
    -Duser.timezone=Asia/Taipei ^
    -Dconsole.encoding=UTF-8 ^
    -Dspring.output.ansi.enabled=never

if %errorlevel% neq 0 (
    echo [錯誤] 應用程式啟動失敗
    pause
    exit /b 1
)

pause 