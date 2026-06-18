package copperhead.client.implement.features.modules.render;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.MultiBooleanSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.implement.events.EventUpdate;

/**
 * Removals (ранее NoRender) — убирает визуальные эффекты для повышения FPS и видимости.
 * Настройки соответствуют скриншоту: 19/24 активных опций.
 */
public class Removals extends Module {

    // === Применять на (основные визуальные эффекты) ===
    public final MultiBooleanSetting applyTo = new MultiBooleanSetting("Применять на",
            new BooleanSetting("Тряска камеры", true),
            new BooleanSetting("Скорборд", true),
            new BooleanSetting("Удочка на экране", true),
            new BooleanSetting("Босс-бар", true),
            new BooleanSetting("Частицы разрушения", true),
            new BooleanSetting("Дождь", true),
            new BooleanSetting("Камера клип", true),
            new BooleanSetting("Тени", true),
            new BooleanSetting("Дым", true),
            new BooleanSetting("Снесение тотема", true),
            new BooleanSetting("Виньетка", true),
            new BooleanSetting("Стрелы в игроке", true),
            new BooleanSetting("Голограммы", true),
            new BooleanSetting("Эффект здоровья", true),
            new BooleanSetting("Трава", true)
    );

    // === Плохие эффекты ===
    public final MultiBooleanSetting badEffects = new MultiBooleanSetting("Плохие эффекты",
            new BooleanSetting("Свечение игроков", true)
    );

    // === Дополнительные опции ===
    public final BooleanSetting players = new BooleanSetting("Игроки", true);
    public final BooleanSetting underwaterBlur = new BooleanSetting("Размытие под водой", true);
    public final BooleanSetting lava = new BooleanSetting("Лава", false);
    public final BooleanSetting fire = new BooleanSetting("Огонь", true);
    public final BooleanSetting titles = new BooleanSetting("Тайтлы", true);
    public final BooleanSetting crystalExplosion = new BooleanSetting("Взрыв кристалла", false);
    public final BooleanSetting boats = new BooleanSetting("Лодки", false);

    // === Уменьшить звук (0/5) ===
    public final MultiBooleanSetting reduceSound = new MultiBooleanSetting("Уменьшить звук",
            new BooleanSetting("Трезубец", false),
            new BooleanSetting("Появление визера", false),
            new BooleanSetting("Открытие энд-портала", false),
            new BooleanSetting("Музыкальные пластинки", false),
            new BooleanSetting("Битье пузырков опыта", false)
    );

    public Removals() {
        super("Removals", "Removals", ModuleCategory.RENDER);
        addSettings(applyTo, badEffects, players, underwaterBlur, lava, fire, titles,
                crystalExplosion, boats, reduceSound);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.level == null) return;

        // Тряска камеры
        if (applyTo.is("Тряска камеры")) {
            mc.player.animationSpeed = 0;
        }

        // Дождь
        if (applyTo.is("Дождь")) {
            mc.level.setRainLevel(0);
        }
    }

    // Проверки для использования в миксинах
    public boolean shouldRemoveBossBar() { return isState() && applyTo.is("Босс-бар"); }
    public boolean shouldRemoveScoreboard() { return isState() && applyTo.is("Скорборд"); }
    public boolean shouldRemoveShadows() { return isState() && applyTo.is("Тени"); }
    public boolean shouldRemoveSmoke() { return isState() && applyTo.is("Дым"); }
    public boolean shouldRemoveTotemAnimation() { return isState() && applyTo.is("Снесение тотема"); }
    public boolean shouldRemoveVignette() { return isState() && applyTo.is("Виньетка"); }
    public boolean shouldRemoveArrowsInPlayer() { return isState() && applyTo.is("Стрелы в игроке"); }
    public boolean shouldRemoveHolograms() { return isState() && applyTo.is("Голограммы"); }
    public boolean shouldRemoveHealthEffect() { return isState() && applyTo.is("Эффект здоровья"); }
    public boolean shouldRemoveGrass() { return isState() && applyTo.is("Трава"); }
    public boolean shouldRemovePlayerGlow() { return isState() && badEffects.is("Свечение игроков"); }
    public boolean shouldRemoveFire() { return isState() && fire.get(); }
    public boolean shouldRemoveTitles() { return isState() && titles.get(); }
    public boolean shouldRemoveUnderwaterBlur() { return isState() && underwaterBlur.get(); }
    public boolean shouldRemoveLava() { return isState() && lava.get(); }
    public boolean shouldRemoveBoats() { return isState() && boats.get(); }
    public boolean shouldRemoveCrystalExplosion() { return isState() && crystalExplosion.get(); }
    public boolean shouldRemoveBreakParticles() { return isState() && applyTo.is("Частицы разрушения"); }
    public boolean shouldRemoveCameraClip() { return isState() && applyTo.is("Камера клип"); }
    public boolean shouldRemoveFishingRod() { return isState() && applyTo.is("Удочка на экране"); }
}
