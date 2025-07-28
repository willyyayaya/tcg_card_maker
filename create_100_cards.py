import pandas as pd
import random

# 定義卡片數據模板
card_types = ["生物", "法術", "裝備", "陷阱"]
rarities = ["普通", "稀有", "史詩", "傳說"]
elements = ["火", "水", "雷", "暗", "草", "光", "冰", "土"]
border_colors = ["#FFD700", "#4169E1", "#FFFF00", "#9400D3", "#32CD32", "#FFA500", "#00FFFF", "#8B4513"]

# 卡片名稱模板
creature_names = [
    "火龍戰士", "水靈法師", "雷電騎士", "暗影刺客", "森林守護者", "光明聖騎", "冰霜巨人", "大地元素",
    "烈焰鳳凰", "深海巨獸", "雷鳥", "暗黑魔王", "樹精長老", "天使戰士", "冰雪女王", "石頭巨人",
    "炎魔領主", "水晶龍", "雷電法師", "影子忍者", "花仙子", "聖光使者", "冰川守衛", "山嶺之王",
    "火焰精靈", "海洋女神", "風暴之子", "夜影盜賊", "自然之母", "神聖騎士", "極地熊王", "岩石巨獸",
    "熔岩巨龍", "珊瑚海妖", "雷霆戰神", "暗夜獵手", "綠葉精靈", "黎明天使", "霜雪狼王", "地底龍王"
]

spell_names = [
    "雷電風暴", "火焰爆發", "冰霜新星", "治療之光", "暗影箭", "自然之怒", "神聖打擊", "地震術",
    "烈焰風暴", "水龍捲", "閃電鏈", "黑暗束縛", "生命綻放", "聖光審判", "冰牢術", "石化凝視",
    "炙熱光線", "潮汐波動", "雷鳴轟擊", "影子分身", "荊棘纏繞", "神聖之盾", "暴雪", "地裂術",
    "流星火雨", "海嘯", "天雷滅世", "暗影風暴", "森林祝福", "天使之翼", "絕對零度", "山崩地裂"
]

equipment_names = [
    "古代遺物", "火焰之劍", "冰霜護甲", "雷電法杖", "暗影斗篷", "生命之樹", "聖光頭盔", "大地之盾",
    "烈焰戰斧", "深海三叉戟", "風暴法杖", "影子匕首", "翡翠項鍊", "光明聖劍", "冰晶護盾", "岩石戰鎚",
    "熔岩戰甲", "珍珠法杖", "雷神之鎚", "暗夜面具", "自然法杖", "天使之劍", "霜雪戰靴", "地心戰甲",
    "鳳凰羽毛", "海神權杖", "雷電護腕", "暗影戒指", "精靈弓箭", "聖騎長槍", "冰龍鱗甲", "泰坦之拳"
]

trap_names = [
    "神聖護盾", "火焰陷阱", "冰霜陷阱", "雷電陷阱", "暗影陷阱", "自然陷阱", "光明陷阱", "大地陷阱",
    "烈焰地獄", "深海漩渦", "雷電牢籠", "暗夜迷霧", "荊棘叢林", "聖光結界", "冰雪封印", "石化陷阱",
    "爆炸陷阱", "水牢術", "電擊陷阱", "幻影陷阱", "毒藤陷阱", "審判之光", "冰錐陷阱", "地震陷阱",
    "烈火焚燒", "龍捲風暴", "天雷轟頂", "黑洞吞噬", "根系束縛", "神罰降臨", "極寒冰封", "山石崩塌"
]

# 圖片URL模板
image_urls = [
    "https://example.com/cards/fire_dragon_warrior.jpg",
    "https://example.com/cards/water_spirit_mage.jpg",
    "https://example.com/cards/thunder_knight.jpg",
    "https://example.com/cards/shadow_assassin.jpg",
    "https://example.com/cards/forest_guardian.jpg",
    "https://example.com/cards/light_paladin.jpg",
    "https://example.com/cards/frost_giant.jpg",
    "https://example.com/cards/earth_elemental.jpg"
]

# 圖片URL和描述將在生成時動態創建

def generate_card_data():
    cards = []
    
    for i in range(100):
        card_type = random.choice(card_types)
        rarity = random.choice(rarities)
        element = random.choice(elements)
        border_color = random.choice(border_colors)
        
        # 根據類型選擇名稱
        if card_type == "生物":
            name = random.choice(creature_names)
        elif card_type == "法術":
            name = random.choice(spell_names)
        elif card_type == "裝備":
            name = random.choice(equipment_names)
        else:  # 陷阱
            name = random.choice(trap_names)
        
        # 生成屬性值
        if card_type == "生物":
            attack = random.randint(1, 10)
            defense = random.randint(1, 10)
        else:
            attack = ""
            defense = ""
        
        cost = random.randint(1, 8)
        
        # 生成描述
        if card_type == "生物":
            descriptions_list = [
                f"強大的{element}系{card_type}，擁有毀滅性的{element}攻擊能力。當此卡片進場時，對所有敵方生物造成{random.randint(1, 3)}點傷害。",
                f"精通{element}系魔法的{card_type}，能夠治療友方生物並控制戰場。每回合開始時，治療一個友方生物{random.randint(1, 3)}點生命值。",
                f"來自{element}元素位面的強大{card_type}，具有{element}屬性的特殊能力。攻擊時有機會觸發{element}效果。",
                f"古老的{element}族{card_type}，擁有悠久的戰鬥經驗。當此生物攻擊時，獲得+{random.randint(1, 2)}/+{random.randint(1, 2)}直到回合結束。"
            ]
        elif card_type == "法術":
            descriptions_list = [
                f"召喚強大的{element}風暴，對所有敵方生物造成傷害。對所有敵方生物造成{random.randint(2, 5)}點傷害。",
                f"釋放{element}的力量，對目標造成巨大傷害。對目標造成{random.randint(3, 6)}點傷害，如果目標死亡，抽一張牌。",
                f"操控{element}元素的奧秘法術，能夠改變戰場局勢。選擇一個目標，使其獲得{element}效果直到回合結束。",
                f"古老的{element}魔法，擁有改變戰局的能力。所有友方生物獲得+{random.randint(1, 2)}/+{random.randint(1, 2)}直到回合結束。"
            ]
        elif card_type == "裝備":
            descriptions_list = [
                f"來自遠古時代的神秘裝備，能夠增強持有者的力量。裝備的生物獲得+{random.randint(1, 3)}/+{random.randint(1, 3)}並且具有踐踏能力。",
                f"由{element}元素鍛造的強大武器，散發著{element}的光芒。裝備的生物攻擊力+{random.randint(1, 3)}，並且攻擊時觸發{element}效果。",
                f"傳說中的{element}裝備，只有真正的勇者才能駕馭。裝備的生物獲得{element}屬性，並且免疫{element}傷害。",
                f"精靈工匠打造的{element}裝備，蘊含著自然的力量。裝備的生物每回合開始時恢復{random.randint(1, 3)}點生命值。"
            ]
        else:  # 陷阱
            descriptions_list = [
                f"為一個友方生物提供{element}的保護，使其免受下一次傷害。目標生物獲得「免疫傷害」直到回合結束。",
                f"隱藏的{element}陷阱，當敵人靠近時觸發。當敵方生物攻擊時，對其造成{random.randint(1, 4)}點傷害。",
                f"設置{element}元素的陷阱，等待敵人踏入。當敵方施放法術時，反彈{random.randint(1, 3)}點傷害給施法者。",
                f"古老的{element}封印，能夠束縛強大的敵人。選擇一個敵方生物，使其無法攻擊{random.randint(1, 2)}回合。"
            ]
        
        description = random.choice(descriptions_list)
        
        # 生成圖片URL
        image_url = f"https://example.com/cards/{element.lower()}_{card_type.lower()}_{i+1:03d}.jpg"
        
        card = {
            "卡片名稱": f"{name}#{i+1:03d}",
            "類型": card_type,
            "稀有度": rarity,
            "攻擊力": attack,
            "防禦力": defense,
            "費用": cost,
            "描述": description,
            "圖片URL": image_url,
            "背景風格": element,
            "邊框顏色": border_color
        }
        
        cards.append(card)
    
    return cards

# 生成卡片數據
cards_data = generate_card_data()

# 創建DataFrame並保存為CSV
df = pd.DataFrame(cards_data)
df.to_csv('test_100_cards.csv', index=False, encoding='utf-8')

# 也保存為Excel格式
df.to_excel('test_100_cards.xlsx', index=False)

print("已生成100筆卡片資料：")
print("- test_100_cards.csv")
print("- test_100_cards.xlsx")
print(f"總共生成了 {len(cards_data)} 筆卡片資料") 