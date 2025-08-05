#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd

# 測試數據，包含本地圖片路徑
data = {
    '名稱': ['火龍戰士', '水晶法師', '暗影刺客'],
    '類型': ['生物', '法術', '生物'],
    '稀有度': ['稀有', '史詩', '傳說'],
    '攻擊力': [8, 5, 9],
    '防禦力': [6, 3, 4],
    '費用': [5, 4, 6],
    '描述': [
        '強大的火龍戰士能夠釋放烈火攻擊敵人',
        '使用水晶的力量來控制戰場',
        '來自暗影界的神秘刺客'
    ],
    '圖片URL': [
        'uploads/images/3084f514-2c64-4caa-92d7-4a6ad193878d.png',
        'uploads/images/3084f514-2c64-4caa-92d7-4a6ad193878d.png', 
        'https://picsum.photos/300/200?random=1'
    ],
    '背景風格': ['火焰', '藍色', '暗色'],
    '邊框顏色': ['紅色', '藍色', '黑色']
}

# 創建DataFrame
df = pd.DataFrame(data)

# 保存為Excel檔案
df.to_excel('test_image_integration.xlsx', index=False, engine='openpyxl')
print("已創建 test_image_integration.xlsx")

# 顯示數據內容
print("\n測試數據內容:")
print(df.to_string(index=False))