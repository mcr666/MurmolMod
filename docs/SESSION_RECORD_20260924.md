# 开发记录 — 2026-09-24（会话整理）

版本：murmol-2.1.1（NeoForge 1.21.1）

## 一、体型与渲染调整

1. **落瓣春花（luohong）整体缩小 0.75x**
   - 采用原版 `Attributes.SCALE` 属性（-0.25 ADD_VALUE），由 `FeralFormManager` 统一挂/卸载，碰撞箱、眼高自动跟随（Pehkui 同源机制）。
   - 第三人称模型渲染乘 `entity.getScale()`；第一人称手臂不改 scale。
   - 碰撞箱水平各方向加宽 1/16 格（每侧），**所有形态通用默认**（`EntityEvent.Size` + `entity.setNewSize()`）。
   - 新增 BODY_Z_OFFSET（向前偏移，方向需游戏内验证）。
   - 模型缩放在 `FeralFormRenderer` 中与 0.015 微缩叠加。

2. **配置项「不显示手臂」**：同时隐藏变形时的原版第一人称手臂（取消 RenderArmEvent）。

## 二、蚕蛾形态（silkmoth，稍后汉化改名「月蛾」）

1. **基础**：配方 幻翼膜 + 蜂蜜瓶 + 萤石粉 + 丝线；悬停飞行（按住空格缓慢上升，速度 0.36 格/tick，松手自然下落；按 `target/0.98+0.08` 预补偿重力阻力）；免摔伤。
2. **灵魂物品**：32×128 四帧精灵动图贴图；各形态物品/贴图/模型层统一文件名格式：`<形态id>_soul`、`<形态id>.png`、层 `<形态id>`（尾部 `<形态id>_tail`）。
3. **专属渲染偏移**：BODY_Y_OFFSET +3.25（`extraBodyYOffset` 字段已删除）。
4. **第一人称手臂动画**：右臂 yRot +45°、左臂 −45°（仅第一人称）。
5. **修复**：每次进游戏/切第三人称冗余播放入场过渡动画 → `FeralActivation.everFeral` 标记，初次渲染即为形态时跳过。
6. **魂之瓶**：右击获得灵魂后添加音效。

## 三、动画系统重构（重点）

1. **各形态专属动画文件**：复制初始 `feral_anim.json` 为
   `assets/murmol/player_animations/{luohong,chen_huang,moss_beast}_anim.json`，
   各 Form 构造器 `setAnimationFile(...)` 指向；human 形态仍回退全局 `astral_cruse:player_animations/feral_anim.json`。
   原始文件备份在 `recycle_bin/`。
2. **尾部动画整合进总动画文件**：代码级 `AnimationDefinition`（furtalsAnimation/leaftailAnimation/TAIL_IDLE）全部废弃，改为各动画文件内命名动画：
   - `tail_idle`（待机循环）、`tail_walk`（行走，按 limbSwingAmount 叠加）。
   - luohong/chen_huang：TailPrimary/Secondary ±2.5°、TailTertiary/Quaternary ±5°（4s）；tail_walk 四骨骼 x −7.5°（0.5s）。
   - moss_beast：Tail/Tail3 ±2.5°、Tail4/Tail5 ±5°；tail_walk 用 leaftail 原始关键帧（2s）。
   - `FeralFormRenderer.applyTailAnimation` 重写：按动画骨骼名在躯干子树查找部件（不再要求 "Tail" 根，月蛾为 bep1），重置后采样叠加。
   - `FeralForm` 基类及各 Form 移除 `getTailIdleAnimation/getTailWalkAnimation`；`leaftailAnimation.java` 移入 recycle_bin；`furtalsAnimation.java` 保留（星辉尾巴盔甲独立模块仍在使用）。
3. **月蛾新模型（偏人形）**：
   - `SilkmothModel` 换用新几何（torso→bone_r1/r2，尾部 bep1 子树，wings，biped_*_leg 人形后腿），128×128 贴图；旧模型/动画/贴图备份 recycle_bin。
   - **未定义骨骼保留原版动画**：曾实现（原版 setupAnim 先执行，仅覆盖定义了通道的核心骨骼）；后按用户要求**撤销**，恢复整体 `resetPlayerParts` + 无 head 通道时 `applyHeadLook` 的原方案。
   - 对现有三形态无影响（其动画各状态均定义全部六骨骼）。
4. **动画器公共 API**（`FeralBedrockPlayerAnimator`）：`animationOf(FeralForm, String...)` / `animationTime` / `boneNames` / `sampleExtraRotation` / `findBone`；`BedrockAnimation` 改为 public record。
5. **历史修复**：
   - Blockbench `{"post":[...],"lerp_mode":"catmullrom"}` 帧格式解析兼容（lerp_mode 按线性处理），否则整个动画文件加载失败；
   - `apply()` 中 head 旋转被测试分支丢弃的 bug；
   - `Mth.degreesToRadians` 在 1.21.1 不存在 → `Math.toRadians`；
   - `ModelPart.children` 为 private → 反射读取（缓存的 `findChild`）。

## 四、其他（本会话早段）

- 手册 landing 文本精简（≤50 字、不透露形态信息，中英）。
- 版本号 2.1.1。
- 临时诊断日志（`debugPose` / `[feral-debug]` / `[feral-debug2]`）仍在代码中，动画问题定位完成后需删除。

## 五、遗留事项

- [ ] 蚕蛾动画问题最终定位（用户已暂停，改由重做动画文件解决）；定位完成后删除临时诊断日志。
- [ ] BODY_Z_OFFSET 偏移方向游戏内验证。
- [ ] 蚕蛾汉化改名「月蛾」。
- [ ] 用户将按初始动画文件格式重做各形态动画名（约定：`tail_idle`/`tail_walk` 命名 + 骨骼名约定）。
- 产物：`build/libs/murmol-2.1.1.jar`（编译通过，2026-09-24）。
