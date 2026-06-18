package copperhead.client.implement.features.modules.render.targetesp;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import copperhead.client.common.util.math.MathUtils;
import copperhead.client.common.util.render.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

public class CircleRenderer extends BaseTargetRenderer {
    private static final Minecraft mc = Minecraft.getInstance();

    private float animatedRadius = 0.0f;
    private float animatedHeight = 0.0f;
    private float animatedAlpha = 0.0f;
    private long lastRenderTime = System.currentTimeMillis();
    private boolean expanding = true;
    private static final float MIN_RADIUS = 0.3f;
    private static final float MAX_RADIUS = 0.8f;
    private static final float RADIUS_SPEED = 0.002f;
    private static final int CIRCLE_SEGMENTS = 64;

    @Override
    public void render(MatrixStack matrixStack, LivingEntity target, float partialTicks) {
        if (mc.player == null || target == null) return;

        long now = System.currentTimeMillis();
        float delta = (now - lastRenderTime);
        lastRenderTime = now;

        // Animate radius pulsation
        if (expanding) {
            animatedRadius += RADIUS_SPEED * delta;
            if (animatedRadius >= MAX_RADIUS) expanding = false;
        } else {
            animatedRadius -= RADIUS_SPEED * delta;
            if (animatedRadius <= MIN_RADIUS) expanding = true;
        }
        animatedRadius = MathUtils.clamp(animatedRadius, MIN_RADIUS, MAX_RADIUS);

        // Animate height oscillation
        float heightOsc = (float) (Math.sin(now / 600.0) * 0.3 + 0.5) * target.getBbHeight();
        animatedHeight += (heightOsc - animatedHeight) * 0.1f;

        // Alpha
        animatedAlpha = 0.6f + 0.3f * (float) Math.sin(now / 400.0);

        // Calculate interpolated position
        double x = MathUtils.interpolate(target.getX(), target.xo, partialTicks);
        double y = MathUtils.interpolate(target.getY(), target.yo, partialTicks);
        double z = MathUtils.interpolate(target.getZ(), target.zo, partialTicks);

        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.pushMatrix();
        RenderSystem.translated(x - cam.x, y - cam.y, z - cam.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        GL11.glLineWidth(2.0f);

        int color = ColorUtils.getColor();
        float[] rgba = ColorUtils.rgba(color);

        // Draw circle at current animated height
        drawCircle(animatedRadius, animatedHeight, rgba[0], rgba[1], rgba[2], animatedAlpha);

        // Draw second smaller circle for inner glow
        drawCircle(animatedRadius * 0.6f, animatedHeight, rgba[0], rgba[1], rgba[2], animatedAlpha * 0.5f);

        // Draw trailing circles
        for (int i = 1; i <= 3; i++) {
            float trailOffset = i * 0.15f;
            float trailAlpha = animatedAlpha * (1.0f - i * 0.25f);
            drawCircle(animatedRadius + i * 0.05f, animatedHeight - trailOffset, rgba[0], rgba[1], rgba[2], trailAlpha);
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void drawCircle(float radius, float height, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / CIRCLE_SEGMENTS;
            float cx = (float) (Math.cos(angle) * radius);
            float cz = (float) (Math.sin(angle) * radius);
            buffer.vertex(cx, height, cz).color(r, g, b, a).endVertex();
        }
        tessellator.end();

        // Draw filled semi-transparent circle
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(0, height, 0).color(r, g, b, a * 0.3f).endVertex();
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / CIRCLE_SEGMENTS;
            float cx = (float) (Math.cos(angle) * radius);
            float cz = (float) (Math.sin(angle) * radius);
            buffer.vertex(cx, height, cz).color(r, g, b, 0.0f).endVertex();
        }
        tessellator.end();
    }

    @Override
    public void reset() {
        animatedRadius = MIN_RADIUS;
        animatedHeight = 0;
        animatedAlpha = 0;
        expanding = true;
    }
}
