# 变形形态面向对象重构实施计划（v3 - 脱离 MCreator）

## 重构目标

彻底推翻 MCreator 生成的"一个形态 = 一套分散 procedure"实现，改为**一个形态 = 一个纯数据类**，所有行为集中在 Manager/Renderer。形态标识改用 **NBT（NeoForge 附件）** 存储，不再依赖 FERAL_TYPE 属性。

---

## 新架构

### 1. 形态数据层（纯数据类）

`FeralForm` 抽象类 — 仅声明数据，无行为：

```java
public abstract class FeralForm {
    private final int id;
    private final ResourceLocation texture;         // 身体纹理（HumanForm 为 null）
    private final ResourceLocation tailTexture;     // 尾巴纹理（null = 同身体）
    private final ModelLayerLocation bodyLayer;     // 身体模型层（null = 原版玩家模型）
    private final ModelLayerLocation tailLayer;     // 尾巴模型层（null = 无尾巴）
    private final Supplier<ItemStack> soulItem;     // 灵魂/预览物品
    private final ResourceLocation advancement;     // 变形成就（null = 无）
    private final Map<Attribute, AttributeModifier> modifiers;
    private final List<ItemStack> transformMaterials;  // 4 个材料，顺序无关
}
```

四个形态子类（构造函数内声明全部数据）：
- `HumanForm` (id=0) — 无模型、无修饰符、无材料
- `LuohongForm` (id=1) — modelfurplayermodel / modelfurtals / luohong.png / 春花之魂 / 成就 witnessof_petal_spring / 材料：花瓣+甜浆果+闪烁西瓜片+苹果
- `ChenHuangForm` (id=2) — model_c_hmodel / model_c_htail / feralch.png / 乘黄之魂 / 成就 get_chenhuang / 材料：金苹果+芒果+糖+蜂蜜瓶
- `MossBeastForm` (id=3) — modelleaf / modelleaftail / furleaf.png + leaftail.png / 苔叶之魂 / 成就 stands_a_loquat_tree / 材料：植之心+苔藓块+藤蔓+孢子花

属性修饰符（统一修饰符 ID `murmol:tf`）：
- Luohong: MAX_HEALTH -4, MOVEMENT_SPEED +0.025, ATTACK_SPEED +0.3, JUMP_STRENGTH +0.2
- ChenHuang: MAX_HEALTH +4, ATTACK_KNOCKBACK +0.5
- MossBeast: MAX_HEALTH +4, ATTACK_KNOCKBACK +0.5, JUMP_STRENGTH +0.2, MOVEMENT_SPEED +0.01, MINING_EFFICIENCY +1

注册表 `FeralForms`：静态实例 + `byId(int)` + `all()`。

### 2. 形态存储（NBT / 附件）

在 `MurmolModVariables.PlayerVariables` 中新增字段 `int feralFormId = 0`，复用现有附件同步机制（`PlayerVariablesSyncMessage`）。
- 读取：`player.getData(PLAYER_VARIABLES).feralFormId`
- 写入：设置后 `markSyncDirty()` 触发同步
- 渲染端（客户端）从同一附件字段读取，保证单一数据源

移除 `MurmolModAttributes` 中的 `FERAL_TYPE` 属性（`ASTRALTAILIO` 保留，用于独立的星幻护甲尾巴）。

### 3. 行为层（Manager，集中所有逻辑）

`FeralFormManager`（`@EventBusSubscriber`，含所有事件监听）：

| 方法/事件 | 职责 |
|---|---|
| `getForm(Entity)` | 读附件 feralFormId → `FeralForms.byId()` |
| `setForm(Entity, FeralForm)` | 写附件 + onExit 旧形态 + clearModifiers + onEnter 新形态 + applyModifiers |
| `applyModifiers(LivingEntity, FeralForm)` | 遍历 modifiers 添加瞬态修饰符 |
| `clearModifiers(LivingEntity)` | 移除所有 `murmol:tf` 修饰符 |
| `transform(level, x, y, z, entity, form, itemstack)` | 完整流程：音效 → setForm → 授予成就 → 失明(60) + 缓慢(60) |
| `findFormByMaterials(entity)` | 遍历 `FeralForms.all()`，用 `CheckMaterialsProcedure` 逻辑匹配 4 格材料 |
| `onPlayerTick(PlayerTickEvent.Post)` | 替代 AttributesettinProcedure：确保当前形态修饰符已应用 |
| `onEquipmentChange(LivingEquipmentChangeEvent)` | 替代 DiaoluokuijProcedure：野性形态下掉落非白名单护甲 |
| `onLivingDamagePre(LivingDamageEvent.Pre)` | 替代 HumankillersjProcedure：人类杀手附魔对人类（form==HUMAN）额外伤害 |

### 4. 渲染层（Renderer + Models，客户端）

`FeralFormModels`（客户端，`EntityRenderersEvent` 监听）：
- `RegisterLayerDefinitions`：注册形态相关模型层（modelfurplayermodel、model_c_hmodel、modelleaf、modelfurtals、model_c_htail、modelleaftail）
- `AddLayers`：烘焙所有形态的 PlayerModel（body）和 EntityModel（tail），存入 `Map<Integer, ...>`
- 提供 `getBodyModel(formId)` / `getTailModel(formId)`

`FeralFormRenderer`（客户端，`Dist.CLIENT`，`@EventBusSubscriber`）：
- `RenderPlayerEvent.Pre`：替代 TransFeralProcedure — 野性形态隐藏原版模型，渲染 `form.getBodyModel()` + `form.getTailModel()`
- `RenderArmEvent`：替代 TffparmProcedure — 渲染形态手臂模型

### 5. 星幻护甲尾巴（独立，不属形态系统）

保留为独立模块 `AstralTailArmorRenderer`：
- `RenderLivingEvent.Pre`：检测胸部护甲是否带 `mod:enableastraltail` 标签，渲染 ASTRAL_TAIL 模型
- `RenderPlayerEvent.Pre`：检测胸甲为 ASTRAL_ARMOR_CHESTPLATE 时渲染 ASTRAL_TAIL
- 纹理 `astralarmor.png`，模型 `modelastraltail`

---

## 文件变更清单

### 新建
| 文件 | 作用 |
|---|---|
| `feral/FeralForm.java` | 形态抽象基类（纯数据） |
| `feral/HumanForm.java` | 人类形态 |
| `feral/LuohongForm.java` | 落瓣春花 |
| `feral/ChenHuangForm.java` | 乘黄 |
| `feral/MossBeastForm.java` | 苔叶兽 |
| `feral/FeralForms.java` | 注册表 |
| `feral/FeralFormManager.java` | 行为中心 + 事件监听 |
| `feral/client/FeralFormModels.java` | 模型层注册 + 烘焙 |
| `feral/client/FeralFormRenderer.java` | 玩家身体 + 手臂渲染 |
| `feral/client/AstralTailArmorRenderer.java` | 星幻护甲尾巴渲染（独立） |

### 修改
| 文件 | 变更 |
|---|---|
| `network/MurmolModVariables.java` | PlayerVariables 新增 `feralFormId` 字段 + 序列化 |
| `init/MurmolModAttributes.java` | 移除 FERAL_TYPE 属性注册，保留 ASTRALTAILIO |
| `init/MurmolModModels.java` | 移除形态相关模型层注册（移入 FeralFormModels），保留实体模型层 |
| `item/TheAstralTomeItem.java` | use() 中直接调用 `FeralFormManager.findFormByMaterials` + `transform`（不再走 procedure） |
| `MurmolMod.java` | 无需改动（Manager/Renderer 用 @EventBusSubscriber 自注册） |

### 删除
所有形态相关 procedure 文件（不再保留）：
- `TfLuohongProcedure`、`TfCHProcedure`、`TfMossProcedure`、`TranhumanProcedure`
- `ChangedTfProcedure`、`CheckMaterialsProcedure`、`DelitemProcedure`
- `AttributesettinProcedure`、`ResetattrProcedure`
- `IfferalProcedure`、`Ifferal0Procedure`、`Ifferal2Procedure`
- `ReturnPreviewItemProcedure`
- `TransFeralProcedure`、`TffparmProcedure`
- `AlfaratthumanProcedure`、`HumankillersjProcedure`、`FireBladeClawGongJuZaiShouShangShiMeiKeFaShengProcedure`
- `DiaoluokuijProcedure`、`ModelregProcedure`
- `MobtailastProcedure`（逻辑移入 AstralTailArmorRenderer）
- `init/MurmolModHumanoidModels.java`、`init/MurmolModAnimatedModels.java`

### 需同步引用更新的文件
- `entity/AlfarEntity.java`、`entity/AstralDrakeEntity.java` — 目标判定改用 `FeralFormManager.getForm(target) == FeralForms.HUMAN`（替代 AlfaratthumanProcedure）
- `item/FireBladeClawItem.java` — onUse 改用 `FeralFormManager.getForm(player) == HUMAN` 判定
- `client/gui/AstralBookScreen.java` — 预览物品改用 `FeralFormManager.getForm(entity).getSoulItem()`
- `item/TotemofFallenItem.java` — finishUsing 调用 `FeralFormManager.transform(..., HUMAN, ...)`

---

## 实施步骤

### 步骤 1：形态数据层
1. 新建 `FeralForm` 抽象类
2. 新建 `HumanForm`、`LuohongForm`、`ChenHuangForm`、`MossBeastForm`
3. 新建 `FeralForms` 注册表

### 步骤 2：存储改造
4. `MurmolModVariables.PlayerVariables` 新增 `feralFormId`，更新 serializeNBT/deserializeNBT
5. `MurmolModAttributes` 移除 FERAL_TYPE

### 步骤 3：行为中心
6. 新建 `FeralFormManager`，实现所有方法与事件监听（tick、装备变更、伤害）
7. 新建 `feral/client/FeralFormModels`（层注册 + 烘焙）
8. 新建 `feral/client/FeralFormRenderer`（身体 + 手臂渲染）
9. 新建 `feral/client/AstralTailArmorRenderer`

### 步骤 4：入口迁移
10. `TheAstralTomeItem.use` → `FeralFormManager.findFormByMaterials` + `transform`
11. `TotemofFallenItem.finishUsing` → `FeralFormManager.transform(HUMAN)`
12. `AstralBookScreen.renderBg` → `FeralFormManager.getForm(entity).getSoulItem()`
13. `AlfarEntity`/`AstralDrakeEntity` 目标判定 → `FeralFormManager.getForm`
14. `FireBladeClawItem` 使用判定 → `FeralFormManager.getForm`

### 步骤 5：清理
15. 删除所有形态 procedure 文件、`MurmolModHumanoidModels`、`MurmolModAnimatedModels`
16. `MurmolModModels` 移除形态层注册
17. 清理 `MurmolModVariables` 中 5 个纹理缓存字段

### 步骤 6：编译验证
18. `./gradlew compileJava`

---

## 关键决策

1. **形态存储用附件 NBT**：复用现有 `PlayerVariables` 附件 + 同步消息，保证客户端渲染与服务端状态单一数据源。FERAL_TYPE 属性完全移除。
2. **形态类纯数据**：不包含任何行为逻辑，行为全部在 `FeralFormManager`。新增形态只需写一个数据类 + 注册一行。
3. **不保留 procedure**：所有事件监听直接写在 Manager/Renderer 的 `@SubscribeEvent` 方法中。
4. **模型层注册**：形态相关模型层注册移入 `FeralFormModels`（RegisterLayerDefinitions + AddLayers 都在这），`MurmolModModels` 仅保留实体模型。
5. **星幻尾巴独立**：作为护甲渲染模块 `AstralTailArmorRenderer`，与形态系统解耦。

## 验证

- `./gradlew compileJava` 通过
- 搜索 `FERAL_TYPE` 无引用
- 搜索 `MurmolModHumanoidModels`、`MurmolModAnimatedModels` 无引用
- 搜索 `CacheFeralPlayerTexture` 等 5 字段无引用
- 搜索 `procedure` 包下已删类名无引用
- 4 种形态变形材料、属性、纹理与原代码逐项核对一致

## 风险

| 风险 | 处理 |
|---|---|
| 附件 feralFormId 同步时序 | 复用现有 markSyncDirty + PlayerVariablesSyncMessage，登录/重生/换维/ tick 变化均同步 |
| 模型烘焙晚于首次渲染 | getBodyModel/getTailModel 从 FeralFormModels 静态 map 取值，AddLayers 事件在首次渲染前触发 |
| 删除 procedure 后其他文件引用 | 步骤 4 同步更新所有入口引用，编译验证兜底 |
