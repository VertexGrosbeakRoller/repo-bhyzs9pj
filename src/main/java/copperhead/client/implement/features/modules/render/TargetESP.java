package copperhead.client.implement.features.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import copperhead.client.CopperHead;
import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.ColorSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.implement.events.EventRender3D;
import copperhead.client.implement.features.modules.combat.Aura;
import copperhead.client.implement.features.modules.render.targetesp.BaseTargetRenderer;
import copperhead.client.implement.features.modules.render.targetesp.CircleRenderer;
import net.minecraft.entity.LivingEntity;

public class TargetESP extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Круг", "Круг");
    private final ColorSetting color = new ColorSetting("Цвет", 0xFF6E44FF);

    private final CircleRenderer circleRenderer = new CircleRenderer();

    public TargetESP() {
        super("TargetESP", "Target ESP", ModuleCategory.RENDER);
        addSettings(mode, color);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.level == null) return;

        // Get target from Aura module
        Aura aura = (Aura) CopperHead.getInstance().getModuleManager().getModule("Aura");
        if (aura == null || !aura.isState()) return;

        LivingEntity target = aura.getTarget();
        if (target == null) return;

        BaseTargetRenderer renderer = circleRenderer;
        renderer.render(e.getMatrixStack(), target, e.getPartialTicks());
    }

    @Override
    public void onDisable() {
        circleRenderer.reset();
        super.onDisable();
    }
}
