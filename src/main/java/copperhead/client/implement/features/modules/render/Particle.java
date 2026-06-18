package copperhead.client.implement.features.modules.render;

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
import copperhead.client.implement.events.EventDamage;
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

public class Particle extends Module {

    private final ModeSetting trigger = new ModeSetting("Триггер", "Удар",
            "Удар", "Передвижение", "Всегда");
    private final SliderSetting amount = new SliderSetting("Количество", 10f, 1f, 50f, 1f);
    private final SliderSetting size = new SliderSetting("Размер", 0.05f, 0.01f, 0.3f, 0.005f);
    private final SliderSetting gravity = new SliderSetting("Гравитация", 0.01f, 0.0f, 0.05f, 0.001f);
    private final SliderSetting speed = new SliderSetting("Скорость разлёта", 0.1f, 0.01f, 0.5f, 0.01f);
    private final SliderSetting lifetime = new SliderSetting("Время жизни", 40f, 10f, 100f, 5f);
    private final BooleanSetting physics = new BooleanSetting("Физика (отскок)", true);
    private final BooleanSetting fadeOut = new BooleanSetting("Затухание", true);
    private final ColorSetting color1 = new ColorSetting("Цвет 1", 0xFFFF4444);
    private final ColorSetting color2 = new ColorSetting("Цвет 2", 0xFFFFAA00);
    private final BooleanSetting rainbow = new BooleanSetting("Радуга", false);

    private final List<ParticlePart> particles = new ArrayList<>();

    public Particle() {
        super("Particle", "Particle", ModuleCategory.RENDER);
        addSettings(trigger, amount, size, gravity, speed, lifetime,
                physics, fadeOut, color1, color2, rainbow);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        if (!trigger.is("Удар")) return;
        if (event.getAttacker() != mc.player) return;

        double tx = event.getTarget().getX();
        double ty = event.getTarget().getY() + event.getTarget().getBbHeight() / 2;
        double tz = event.getTarget().getZ();
        spawnParticles(tx, ty, tz);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.level == null) return;

        if (trigger.is("Передвижение") && (mc.player.getDeltaMovement().lengthSqr() > 0.001)) {
            if (mc.player.tickCount % 3 == 0) {
                spawnParticles(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            }
        } else if (trigger.is("Всегда") && mc.player.tickCount % 5 == 0) {
            spawnParticles(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ());
        }

        float partialTicks = e.getPartialTicks();
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        Iterator<ParticlePart> it = particles.iterator();
        while (it.hasNext()) {
            ParticlePart p = it.next();
            p.update();

            if (p.age > lifetime.get() || p.alpha <= 0) {
                it.remove();
                continue;
            }

            float s = size.get() * p.sizeMultiplier;
            double rx = p.x - cam.x;
            double ry = p.y - cam.y;
            double rz = p.z - cam.z;

            int c;
            if (rainbow.get()) {
                c = java.awt.Color.HSBtoRGB(((System.currentTimeMillis() + p.age * 50L) % 3600) / 3600f, 0.8f, 1.0f);
            } else {
                float lerpFactor = (float) p.age / lifetime.get();
                c = ColorUtils.overCol(color1.get(), color2.get(), lerpFactor);
            }
            float[] rgba = ColorUtils.rgba(c);

            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            buffer.vertex(rx - s, ry - s, rz).color(rgba[0], rgba[1], rgba[2], p.alpha).endVertex();
            buffer.vertex(rx - s, ry + s, rz).color(rgba[0], rgba[1], rgba[2], p.alpha).endVertex();
            buffer.vertex(rx + s, ry - s, rz).color(rgba[0], rgba[1], rgba[2], p.alpha).endVertex();
            buffer.vertex(rx + s, ry + s, rz).color(rgba[0], rgba[1], rgba[2], p.alpha).endVertex();
            tessellator.end();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void spawnParticles(double x, double y, double z) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < (int) amount.get().floatValue(); i++) {
            particles.add(new ParticlePart(
                    x + rand.nextDouble(-0.3, 0.3),
                    y + rand.nextDouble(-0.1, 0.3),
                    z + rand.nextDouble(-0.3, 0.3)
            ));
        }
    }

    @Override
    public void onDisable() {
        particles.clear();
        super.onDisable();
    }

    private class ParticlePart {
        double x, y, z;
        double motionX, motionY, motionZ;
        float alpha;
        float sizeMultiplier;
        int age;

        ParticlePart(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            double sp = speed.get();
            this.motionX = rand.nextDouble(-sp, sp);
            this.motionY = rand.nextDouble(0, sp * 1.5);
            this.motionZ = rand.nextDouble(-sp, sp);
            this.alpha = 1.0f;
            this.sizeMultiplier = rand.nextFloat() * 0.5f + 0.75f;
            this.age = 0;
        }

        void update() {
            age++;
            x += motionX;
            y += motionY;
            z += motionZ;
            motionY -= gravity.get();

            if (physics.get() && y <= mc.player.getY() && motionY < 0) {
                motionY = -motionY * 0.5;
                motionX *= 0.7;
                motionZ *= 0.7;
            }

            motionX *= 0.98;
            motionZ *= 0.98;

            if (fadeOut.get()) {
                float lifeRatio = (float) age / lifetime.get();
                if (lifeRatio > 0.5f) {
                    alpha = 1.0f - (lifeRatio - 0.5f) * 2.0f;
                }
            }
            alpha = MathUtils.clamp(alpha, 0.0f, 1.0f);
        }
    }
}
