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
import copperhead.client.common.util.math.MathUtils;
import copperhead.client.common.util.render.ColorUtils;
import copperhead.client.implement.events.EventRender3D;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

public class Cosmetics extends Module {

    // Demon wing outline (only demon wings as per request)
    private static final float[][] DEMON_WING = {
            {0.0f, 0.0f}, {-0.1f, 0.3f}, {-0.3f, 0.6f}, {-0.5f, 0.9f},
            {-0.7f, 1.1f}, {-0.9f, 1.2f}, {-1.0f, 1.0f}, {-0.85f, 0.7f},
            {-0.9f, 0.5f}, {-0.7f, 0.3f}, {-0.6f, 0.1f}, {-0.3f, -0.1f},
            {0.0f, 0.0f}
    };

    private static final float[][] ANGEL_WING = {
            {0.0f, 0.0f}, {-0.05f, 0.2f}, {-0.15f, 0.5f}, {-0.3f, 0.8f},
            {-0.5f, 1.0f}, {-0.7f, 1.1f}, {-0.9f, 1.0f}, {-1.0f, 0.8f},
            {-0.95f, 0.5f}, {-0.8f, 0.3f}, {-0.6f, 0.1f}, {-0.3f, -0.05f},
            {0.0f, 0.0f}
    };

    private static final float[][] PHANTOM_WING = {
            {0.0f, 0.0f}, {-0.2f, 0.4f}, {-0.4f, 0.7f}, {-0.5f, 0.9f},
            {-0.7f, 1.2f}, {-1.0f, 1.4f}, {-1.2f, 1.2f}, {-1.1f, 0.9f},
            {-0.9f, 0.6f}, {-0.7f, 0.3f}, {-0.4f, 0.1f}, {-0.2f, -0.1f},
            {0.0f, 0.0f}
    };

    private final ModeSetting wingType = new ModeSetting("Тип крыльев", "Демонические",
            "Демонические", "Ангельские", "Фантом");
    private final ColorSetting wingColor = new ColorSetting("Цвет крыльев", 0xFFFF0000);
    private final SliderSetting scale = new SliderSetting("Размер", 1.0f, 0.3f, 2.0f, 0.1f);
    private final BooleanSetting onlyFirstPerson = new BooleanSetting("Только от 3 лица", false);
    private final BooleanSetting animated = new BooleanSetting("Анимация", true);
    private final SliderSetting flapSpeed = new SliderSetting("Скорость взмаха", 1.0f, 0.1f, 3.0f, 0.1f);

    private float wingAngle = 0.0f;
    private float flapPhase = 0.0f;

    public Cosmetics() {
        super("Cosmetics", "Cosmetics", ModuleCategory.RENDER);
        addSettings(wingType, wingColor, scale, onlyFirstPerson, animated, flapSpeed);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.world == null) return;
        if (onlyFirstPerson.get() && mc.gameSettings.getPointOfView().func_243192_a() == 0) return;

        float partialTicks = e.getPartialTicks();

        // Wing animation
        if (animated.get()) {
            flapPhase += 0.05f * flapSpeed.get();
            wingAngle = (float) Math.sin(flapPhase) * 20.0f;
            if (mc.player.isElytraFlying()) {
                wingAngle = (float) Math.sin(flapPhase * 2.0f) * 35.0f;
            }
        } else {
            wingAngle = 0.0f;
        }

        double x = MathUtils.interpolate(mc.player.getPosX(), mc.player.prevPosX, partialTicks);
        double y = MathUtils.interpolate(mc.player.getPosY(), mc.player.prevPosY, partialTicks);
        double z = MathUtils.interpolate(mc.player.getPosZ(), mc.player.prevPosZ, partialTicks);
        Vector3d cam = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        float yaw = mc.player.rotationYaw;
        float s = scale.get();

        float[][] wing = getWingPoints();
        int color = wingColor.get();
        float[] rgba = ColorUtils.rgba(color);

        RenderSystem.pushMatrix();
        RenderSystem.translated(x - cam.x, y - cam.y + mc.player.getEyeHeight() - 0.2, z - cam.z);
        RenderSystem.rotatef(-yaw, 0, 1, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glLineWidth(2.0f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Left wing
        RenderSystem.pushMatrix();
        RenderSystem.translated(-0.15, 0, 0.15);
        RenderSystem.rotatef(wingAngle, 0, 0, 1);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        for (float[] point : wing) {
            buffer.pos(point[0] * s, point[1] * s, 0).color(rgba[0], rgba[1], rgba[2], 0.7f).endVertex();
        }
        tessellator.draw();

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (float[] point : wing) {
            buffer.pos(point[0] * s, point[1] * s, 0).color(rgba[0], rgba[1], rgba[2], 1.0f).endVertex();
        }
        tessellator.draw();
        RenderSystem.popMatrix();

        // Right wing (mirrored)
        RenderSystem.pushMatrix();
        RenderSystem.translated(0.15, 0, 0.15);
        RenderSystem.rotatef(-wingAngle, 0, 0, 1);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        for (float[] point : wing) {
            buffer.pos(-point[0] * s, point[1] * s, 0).color(rgba[0], rgba[1], rgba[2], 0.7f).endVertex();
        }
        tessellator.draw();

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (float[] point : wing) {
            buffer.pos(-point[0] * s, point[1] * s, 0).color(rgba[0], rgba[1], rgba[2], 1.0f).endVertex();
        }
        tessellator.draw();
        RenderSystem.popMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private float[][] getWingPoints() {
        switch (wingType.get()) {
            case "Ангельские": return ANGEL_WING;
            case "Фантом": return PHANTOM_WING;
            case "Демонические":
            default: return DEMON_WING;
        }
    }
}
