package copperhead.client.implement.features.modules.render;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.ColorSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.implement.events.EventUpdate;

/**
 * Ambience — изменяет время суток, цвет тумана и неба.
 *
 * Шейдеры, используемые в режимах неба:
 * - Космос (Space): "shaders/post/space.json" — фрагментный шейдер "program/space.fsh"
 * - Плазма (Plasma): "shaders/post/plasma.json" — фрагментный шейдер "program/plasma.fsh"
 * - Balatro: "shaders/post/balatro.json" — фрагментный шейдер "program/balatro.fsh"
 * - Лето (Summer): без шейдера, изменяет цвет тумана + время суток
 * - Сакура (Sakura): без шейдера, изменяет цвет тумана (розовый)
 *
 * Все кастомные шейдеры находятся в:
 * assets/javelin/shaders/post/*.json и assets/javelin/shaders/program/*.fsh
 */
public class Ambience extends Module {

    public static ModeSetting skyMode = new ModeSetting("Небо", "Обычное",
            "Обычное", "Космос", "Плазма", "Balatro", "Лето", "Сакура");

    private final ModeSetting timeMode = new ModeSetting("Время", "Реальное",
            "Реальное", "День", "Ночь", "Закат", "Кастомное");
    private final SliderSetting customTime = new SliderSetting("Кастомное время", 6000f, 0f, 24000f, 100f)
            .setVisible(() -> timeMode.is("Кастомное"));

    private final ColorSetting fogColor = new ColorSetting("Цвет тумана", 0xFF87CEEB);
    private final SliderSetting fogDensity = new SliderSetting("Плотность тумана", 0.0f, 0.0f, 1.0f, 0.01f);

    public Ambience() {
        super("Ambience", "Ambience", ModuleCategory.RENDER);
        addSettings(skyMode, timeMode, customTime, fogColor, fogDensity);
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;

        // Time manipulation
        if (!timeMode.is("Реальное")) {
            long time;
            switch (timeMode.get()) {
                case "День": time = 1000; break;
                case "Ночь": time = 18000; break;
                case "Закат": time = 12500; break;
                case "Кастомное": time = customTime.get().longValue(); break;
                default: time = mc.level.getDayTime(); break;
            }
            mc.level.setDayTime(time);
        }
    }

    public String getActiveShaderName() {
        switch (skyMode.get()) {
            case "Космос": return "shaders/post/space.json";
            case "Плазма": return "shaders/post/plasma.json";
            case "Balatro": return "shaders/post/balatro.json";
            default: return null;
        }
    }

    public boolean hasSkyShader() {
        return getActiveShaderName() != null;
    }
}
