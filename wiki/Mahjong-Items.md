# 麻将物品 (Mahjong Items)

> 立直麻雀内容整合页。注册 id 在 `richi` 命名空间下。详细玩法见 [Systems](Systems)。

## 风盘 (Feng Pan)

| 方块 | id | 说明 |
|---|---|---|
| 风盘 | `richi:fengpan` | 2×2 麻雀桌。右键加入牌局 / 排队；游戏大厅可设置座位（AI）、牌风、对局类型、明牌、思考时限、牌谱回放等 |

- 支持对局类型：一局 / 东风战 / 半庄战。
- AI 牌风：随机 / 一般流 / 野猪流 / 门清流 / 御无双 / 魂天流 / 闪电流。
- AI 形象：春花 / 乘黄 / 苔叶兽 / 月蛾 / 狛犬 / 文鳐 / 狰 / 人类 / 村民。
- 点歌：手持任意唱片右击风盘，向 32 格内广播播唱片音效。
- 相关指令：`/murmol mahjong refresh` 清理桌面实体。

## 麻雀牌 (Mahjong Tiles)

| 牌 | id 规则 | 说明 |
|---|---|---|
| 万子 | `richi:mahjong_1m`~`9m` | 一万 ~ 九万 |
| 饼子 | `richi:mahjong_1p`~`9p` | 一饼 ~ 九饼 |
| 索子 | `richi:mahjong_1s`~`9s` | 一索 ~ 九索 |
| 字牌 | `richi:mahjong_1z`~`7z` | 东 / 南 / 西 / 北 / 中 / 发 / 白 |
| 红五 | `richi:mahjong_0m`/`0p`/`0s` | 红五万 / 红五饼 / 红五索 |
| 未知牌 | `richi:mahjong_tile_unknown` | 牌背/未知 |
| 麻雀牌 | `richi:mahjong_tile` | 通用牌物品 |

## 点棒 (Point Sticks)

| 物品 | id | 说明 |
|---|---|---|
| 千点棒 | `richi:mahjong_stick_1000` | 千点棒（另有 100 / 5000 / 10000 点棒模型） |

## 牌谱 (Paipu)

- 牌谱回放：大厅可查看牌谱、本地牌谱（游戏根目录 `richi/`）、上传/下载到服务器。
- 结算：宝牌 / 里宝 / 满贯 / 跳满 / 倍满 / 三倍满 / 役满等。

---
*详细牌局机制见 [Systems](Systems) 与仓库 `docs/MAHJONG_PORT.md`。*
