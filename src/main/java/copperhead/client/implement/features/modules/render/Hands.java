package copperhead.client.implement.features.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ColorSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.render.ColorUtils;
import copperhead.client.implement.events.EventRender3D;

public class Hands extends Module {

    private final ModeSetting fillMode = new ModeSetting("Режим заливки", "Static", "Static", "Wave", "Outline");
    private final ColorSetting mainColor = new ColorSetting("Основной цвет", 0xFF6E44FF);
    private final ColorSetting secondColor = new ColorSetting("Второй цвет", 0xFF00FFFF)
            .setVisible(() -> fillMode.is("Wave"));
    private final SliderSetting waveSpeed = new SliderSetting("Скорость волны", 1.0f, 0.1f, 5.0f, 0.1f)
            .setVisible(() -> fillMode.is("Wave"));
    private final SliderSetting alpha = new SliderSetting("Прозрачность", 0.8f, 0.1f, 1.0f, 0.05f);
    private final BooleanSetting onlyInHand = new BooleanSetting("Только с предметом", false);
    private final BooleanSetting glint = new BooleanSetting("Блеск", true);
    private final SliderSetting scaleX = new SliderSetting("Масштаб X", 1.0f, 0.5f, 2.0f, 0.05f);
    private final SliderSetting scaleY = new SliderSetting("Масштаб Y", 1.0f, 0.5f, 2.0f, 0.05f);
    private final SliderSetting posX = new SliderSetting("Позиция X", 0.0f, -1.0f, 1.0f, 0.05f);
    private final SliderSetting posY = new SliderSetting("Позиция Y", 0.0f, -1.0f, 1.0f, 0.05f);

    public Hands() {
        super("Hands", "Hands", ModuleCategory.RENDER);
        addSettings(fillMode, mainColor, secondColor, waveSpeed, alpha, onlyInHand, glint, scaleX, scaleY, posX, posY);
    }

    public int getHandColor(float time) {
        int color = mainColor.get();
        if (fillMode.is("Wave")) {
            float wave = (float) (Math.sin(time * waveSpeed.get()) * 0.5 + 0.5);
            color = ColorUtils.overCol(mainColor.get(), secondColor.get(), wave);
        }
        return ColorUtils.multAlpha(color, alpha.get());
    }

    public boolean isOutlineMode() {
        return fillMode.is("Outline");
    }

    public boolean isGlintEnabled() {
        return glint.get();
    }

    public float getScaleX() { return scaleX.get(); }
    public float getScaleY() { return scaleY.get(); }
    public float getPosX() { return posX.get(); }
    public float getPosY() { return posY.get(); }

    public boolean shouldRender() {
        if (mc.player == null) return false;
        if (onlyInHand.get() && mc.player.getHeldItemMainhand().isEmpty() && mc.player.getHeldItemOffhand().isEmpty())
            return false;
        return true;
    }
}
