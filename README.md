# CopperHead Client

Minecraft 1.16.5 Forge мод-клиент с модулями для PvP, движения, рендера и утилит.

## Структура проекта

```
src/main/java/
├── copperhead/client/
│   ├── CopperHead.java                    # Главный класс клиента
│   ├── api/
│   │   ├── event/                         # Система событий (EventBus, EventHandler)
│   │   └── feature/
│   │       ├── module/                    # Базовый Module, ModuleCategory
│   │       └── setting/                   # Настройки (Boolean, Mode, Slider, Color, String, Button, MultiBool, Bind)
│   ├── common/util/
│   │   ├── entity/                        # MoveUtils, InventoryUtils, DamageUtil
│   │   ├── math/                          # MathUtils, GCDUtil, TimerUtils, AnimationUtil
│   │   └── render/                        # ColorUtils
│   ├── implement/
│   │   ├── events/                        # Все события (EventUpdate, EventPacket, EventMotion, EventRender3D, ...)
│   │   └── features/modules/
│   │       ├── combat/                    # Aura, SuperBow, AntiThorns, AntiBot, Snap, FunTimeRotation
│   │       ├── movement/                  # Speed, NoSlow, GrimGlide, ElytraMotion, HighJump
│   │       ├── render/                    # Removals, TargetESP, Cosmetics, NameTags, Ambience, Hands,
│   │       │                              # AncientDebris, FireFlies, Particle
│   │       ├── player/                    # NameProtect, AutoTool
│   │       └── misc/                      # FriendCord, ServerHelper, PlayerSounds
│   └── managers/                          # ModuleManager, FriendManager
├── baritone/                              # Baritone pathfinding (исходный код, НЕ мод)
│   ├── api/                               # BaritoneAPI, IBaritone, Settings
│   ├── pathing/                           # PathingBehavior, MineProcess, FollowProcess, GoToProcess
│   └── command/                           # CommandManager
```

## Модули

### Combat
- **Aura** — КА с ротацией FunTime/Snap/None
- **SuperBow** — Ускоренная стрельба из лука с полной зарядкой
- **AntiThorns** — Отмена дамага от шипов при полёте на элитре
- **AntiBot** — Обнаружение ботов (ReallyWorld/Matrix/UniAC)
- **Snap** — Лёгкая тряска головы + наведение при ударе

### Movement
- **Speed** — 7 режимов (Strafe, StrafeStrict, Motion, Matrix, GrimCollision, MetaHvH, HolyWorld)
- **NoSlow** — 6 режимов (ReallyWorld, Grim, Matrix, Обычный, GrimTick, LonyGrief)
- **GrimGlide** — Глайд на элитре с подъёмом вверх (ReallyWorld/LonyGrief)
- **ElytraMotion** — Автоматические фейерверки при полёте
- **HighJump** — Высокий прыжок (Elytra/Shulker/Elytra2)

### Render
- **Removals** (ex NoRender) — Удаление визуальных эффектов (19 настроек)
- **TargetESP** — Круговой рендер вокруг цели Ауры
- **Cosmetics** — Крылья на спине (Демонические/Ангельские/Фантом)
- **NameTags** — 2 режима: Стандартный и Расширенный (HP, дистанция, пинг, друзья)
- **Ambience** — Управление временем, небом, туманом (шейдеры: space.fsh, plasma.fsh, balatro.fsh)
- **Hands** — Эффекты рук (Static/Wave/Outline)
- **AncientDebris** — Подсветка незеритовых обломков через стены
- **FireFlies** — Светлячки вокруг игрока
- **Particle** — Частицы при ударе/движении/всегда

### Player
- **NameProtect** — Замена ника на copperhead.fun
- **AutoTool** — Авто-выбор инструмента + свап из инвентаря + авто-зелье

### Misc
- **FriendCord** — Отправка координат друзьям через /msg
- **ServerHelper** — Утилиты для серверов (FunTime/HolyWorld/ReallyWorld/LonyGrief)
- **PlayerSounds** — Воспроизведение звуков из папки ~/CopperHead/sounds/

## Baritone (источники)
Baritone интегрирован как исходный код. Команды через чат с префиксом `#`:
- `#goto X Y Z` — идти к координатам
- `#mine diamond_ore` — копать руду
- `#follow PlayerName` — следовать за игроком
- `#cancel` / `#stop` — остановить всё

## Сборка
```bash
./gradlew build
```

## Шейдеры (для Ambience)
- `assets/javelin/shaders/post/space.json` → `program/space.fsh`
- `assets/javelin/shaders/post/plasma.json` → `program/plasma.fsh`
- `assets/javelin/shaders/post/balatro.json` → `program/balatro.fsh`
