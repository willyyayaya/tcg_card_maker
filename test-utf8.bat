@echo off
chcp 65001
echo 測試繁體中文顯示
echo 正在啟動TCG卡片製作工具...

set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=TW
set MAVEN_OPTS=-Dfile.encoding=UTF-8

echo 環境變數設定完成
echo 正在啟動應用程式...

mvn spring-boot:run -Dfile.encoding=UTF-8
pause 