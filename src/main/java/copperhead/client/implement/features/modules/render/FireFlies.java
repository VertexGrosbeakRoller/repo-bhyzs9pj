package copperhead.client.implement.features.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ColorSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.math.MathUtils;
import copperhead.client.common.util.render.ColorUtils;
import copperhead.client.implement.events.EventRender3D;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * FireFlies — светлячки/частицы вокруг игрока.
 * Текстуры: assets/javelin/Particle/firepart.png, assets/javelin/textures/bloom/bloom.png
 */
public class FireFlies extends Module {

    private final SliderSetting amount = new SliderSetting("Количество", 30f, 5f, 100f, 1f);
    private final SliderSetting radius = new SliderSetting("Радиус", 5.0f, 1.0f, 15.0f, 0.5f);
    private final SliderSetting particleSize = new SliderSetting("Размер частиц", 0.05f, 0.01f, 0.2f, 0.005f);
    private final SliderSetting speed = new SliderSetting("Скорость", 0.02f, 0.005f, 0.1f, 0.005f);
    private final ColorSetting color1 = new ColorSetting("Цвет 1", 0xFFFFFF00);
    private final ColorSetting color2 = new ColorSetting("Цвет 2", 0xFFFF8800);
    private final BooleanSetting bloom = new BooleanSetting("Свечение", true);
    private final SliderSetting lifeTime = new SliderSetting("Время жизни (тики)", 200f, 50f, 500f, 10f);

    private final List<FirePart> FIRE_PARTS_LIST = new ArrayList<>();

    public FireFlies() {
        super("FireFlies", "Fire Flies", ModuleCategory.RENDER);
        addSettings(amount, radius, particleSize, speed, color1, color2, bloom, lifeTime);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.level == null) return;

        float partialTicks = e.getPartialTicks();
        double playerX = MathUtils.interpolate(mc.player.getX(), mc.player.xo, partialTicks);
        double playerY = MathUtils.interpolate(mc.player.getY(), mc.player.yo, partialTicks) + 1.0;
        double playerZ = MathUtils.interpolate(mc.player.getZ(), mc.player.zo, partialTicks);

        // Spawn new particles
        while (FIRE_PARTS_LIST.size() < (int) amount.get().floatValue()) {
            FIRE_PARTS_LIST.add(new FirePart(playerX, playerY, playerZ));
        }

        // Update and render
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        Iterator<FirePart> it = FIRE_PARTS_LIST.iterator();
        while (it.hasNext()) {
            FirePart part = it.next();
            part.update();

            if (part.age > lifeTime.get() || part.alpha <= 0) {
                it.remove();
                continue;
            }

            float size = particleSize.get() * part.sizeMultiplier;
            double rx = part.x - cam.x;
            double ry = part.y - cam.y;
            double rz = part.z - cam.z;

            float lerpFactor = (float) part.age / lifeTime.get();
            int c = ColorUtils.overCol(color1.get(), color2.get(), lerpFactor);
            float[] rgba = ColorUtils.rgba(c);

            // Draw as point/small quad
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            buffer.vertex(rx - size, ry - size, rz).color(rgba[0], rgba[1], rgba[2], part.alpha).endVertex();
            buffer.vertex(rx - size, ry + size, rz).color(rgba[0], rgba[1], rgba[2], part.alpha).endVertex();
            buffer.vertex(rx + size, ry - size, rz).color(rgba[0], rgba[1], rgba[2], part.alpha).endVertex();
            buffer.vertex(rx + size, ry + size, rz).color(rgba[0], rgba[1], rgba[2], part.alpha).endVertex();
            tessellator.end();

            // Bloom glow effect
            if (bloom.get()) {
                float bloomSize = size * 3.0f;
                buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                buffer.vertex(rx - bloomSize, ry - bloomSize, rz).color(rgba[0], rgba[1], rgba[2], part.alpha * 0.2f).endVertex();
                buffer.vertex(rx - bloomSize, ry + bloomSize, rz).color(rgba[0], rgba[1], rgba[2], part.alpha * 0.2f).endVertex();
                buffer.vertex(rx + bloomSize, ry - bloomSize, rz).color(rgba[0], rgba[1], rgba[2], part.alpha * 0.2f).endVertex();
                buffer.vertex(rx + bloomSize, ry + bloomSize, rz).color(rgba[0], rgba[1], rgba[2], part.alpha * 0.2f).endVertex();
                tessellator.end();
            }
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    @Override
    public void onDisable() {
        FIRE_PARTS_LIST.clear();
        super.onDisable();
    }

    private class FirePart {
        double x, y, z;
        double motionX, motionY, motionZ;
        float alpha;
        float sizeMultiplier;
        int age;
        double phaseOffset;

        FirePart(double originX, double originY, double originZ) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            double r = radius.get();
            this.x = originX + rand.nextDouble(-r, r);
            this.y = originY + rand.nextDouble(-r / 2, r / 2);
            this.z = originZ + rand.nextDouble(-r, r);
            this.motionX = rand.nextDouble(-0.01, 0.01);
            this.motionY = rand.nextDouble(0.005, 0.02);
            this.motionZ = rand.nextDouble(-0.01, 0.01);
            this.alpha = rand.nextFloat() * 0.5f + 0.5f;
            this.sizeMultiplier = rand.nextFloat() * 0.5f + 0.75f;
            this.age = 0;
            this.phaseOffset = rand.nextDouble(Math.PI * 2);
        }

        void update() {
            age++;
            float s = speed.get();
            x += motionX * s * 20;
            y += motionY * s * 20;
            z += motionZ * s * 20;

            // Sinusoidal wandering
            x += Math.sin(age * 0.1 + phaseOffset) * 0.005;
            z += Math.cos(age * 0.1 + phaseOffset) * 0.005;

            // Fade out towards end of life
            float lifeRatio = (float) age / lifeTime.get();
            if (lifeRatio > 0.7f) {
                alpha -= 0.02f;
            }

            // Flicker
            if (age % 10 == 0) {
                alpha += ThreadLocalRandom.current().nextFloat() * 0.1f - 0.05f;
            }
            alpha = MathUtils.clamp(alpha, 0.0f, 1.0f);
        }
    }
}
