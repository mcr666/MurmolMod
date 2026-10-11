# 系统机制 (Systems)

> 全部系统/机制整合页。详细实现见仓库 `docs/PROJECT_MEMORY.md` 与 `docs/CONTENT_ARCHIVE.md`。

## 变形机制 (Transformation)
`FeralFormManager` + `FeralForms`。三种变形途径（形态之魂 / 唤灵台 / 返归人类 + 照妖镜）；属性修饰符随环境增减；变身附加失明+创造冲击+缓慢 3 秒。详见 [Forms](Forms)。

## 两仪 (Two Principles)
`two_principles` 方块管理各形态启用状态；世界级 SavedData + 网络同步；管理员（权限 3）可编辑；禁用形态无法信物变形、指令绕过。

## 石化 (Petrify)
`murmol:petrify` 效果。触发：踩/挖诅咒石头、石化药水、狛犬石像。生物贴图替换依赖 `petrified_state` 同步附件；三路清除（过期/牛奶/逐 tick 自愈）；交互锁统一 `PetrifyLockHandlers.isLocked`；所有石化类效果不显示粒子；狛犬免疫。

## 显形 (Reveal)
`murmol:reveal` 效果。触发：右键照妖镜方块（10s）、显形药水（1min / +红石 5min）。首次记录原形态并临时替换（变形者→人类，人类→两仪启用中的随机形态）；结束复原；期间灵魂物品与照妖镜不可用。

## 囚笼 (Cage)
`cage` 方块。红石激活禁锢踩笼顶实体（单笼 1 个，缩放至 0.75 高）；断电弹出、破坏释放；被困者挖脚下自救（等效黑曜石）。

## Astral Core 冲击
有重力弹射物，命中/5 秒后触发：闪电 + 无方块破坏爆炸（强度 3）+ 同心圆收缩粒子 + 音效；半径 4 内实体上抛并获星幻感染 5 秒。

## 星幻感染 (Astral Infection)
`murmol:astral_infection`。每 20 tick 结算 `1.0×(amplifier+1)` 伤害；死亡信息「被溶蚀殆尽」。

## 诅咒石头 (Cursed Stone)
`cursed_stone` 方块。踩上/挖掘触发石化；挖掘碎裂并掉落。

## 星幻群系与结构
- **星幻瘟疫群系**（`astral_infection_biome`）：星幻土/幻星石地表，幻星树、星界幼龙大量生成。
- **结构**：幻星废墟（`astruinszlp`）、猫哭冢（`maocryscuptre`）——残页与量子态附魔来源；麻雀屋（`mahjong_house`）——含唱片与麻雀桌。

## 麻雀 (Mahjong)
`richi` 命名空间。风盘 2×2 麻雀桌、四人牌局（东南西北、25000 点起步）、NPC 玩家、牌谱回放、点歌、段位排行（天凤 pt）。详见 [Mahjong Items](Mahjong-Items)。

## 附魔 (Enchantments)
- **量子态**（`murmol:quantum_state`）：变形时护甲不脱落；图书管理员 4 级交易（12 绿宝石）+ 结构宝箱 5%。
- **人类杀手**（`murmol:human_killer`）：对「两脚兽」增伤。

## 效果与药水 (Effects & Potions)
- 效果：石化 / 显形 / 星幻感染 / 创意震撼 / 寒冷刺骨。
- 药水：石化（粗制+石头，30s）、显形（粗制+摩摩尔之魂，1min；+红石 5min）；支持普通/喷溅/滞留/药水箭。

## JEI 集成
软依赖。`feral_transform` 配方类别展示唤灵台变形，催化剂为幻星秘典。

## 本地化
简体中文 + 英文双语，键集合一致；帕秋莉手册双语对应；麻雀相关统一用词。

---
*完整开发约定与版本历史见仓库 `docs/`。*
