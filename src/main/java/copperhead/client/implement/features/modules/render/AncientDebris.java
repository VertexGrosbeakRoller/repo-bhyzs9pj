package copperhead.client.implement.features.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ColorSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.render.ColorUtils;
import copperhead.client.implement.events.EventRender3D;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class AncientDebris extends Module {

    private final SliderSetting searchRadius = new SliderSetting("Радиус поиска", 32f, 8f, 64f, 1f);
    private final ColorSetting outlineColor = new ColorSetting("Цвет обводки", 0xFFFF6600);
    private final ColorSetting fillColor = new ColorSetting("Цвет заливки", 0x40FF6600);
    private final BooleanSetting showNetheriteOre = new BooleanSetting("Показать незритовые обломки", true);
    private final BooleanSetting tracerLines = new BooleanSetting("Линии до блоков", true);
    private final SliderSetting lineWidth = new SliderSetting("Толщина линий", 1.5f, 0.5f, 5.0f, 0.5f);

    private final List<BlockPos> foundBlocks = new ArrayList<>();
    private int scanTick = 0;

    public AncientDebris() {
        super("AncientDebris", "Ancient Debris", ModuleCategory.RENDER);
        addSettings(searchRadius, outlineColor, fillColor, showNetheriteOre, tracerLines, lineWidth);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.world == null) return;

        // Scan every 20 ticks
        scanTick++;
        if (scanTick >= 20) {
            scanTick = 0;
            scanForDebris();
        }

        float partialTicks = e.getPartialTicks();
        Vector3d cam = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glLineWidth(lineWidth.get());

        int outColor = outlineColor.get();
        int fColor = fillColor.get();
        float[] outRGBA = ColorUtils.rgba(outColor);
        float[] fillRGBA = ColorUtils.rgba(fColor);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (BlockPos pos : foundBlocks) {
            double x = pos.getX() - cam.x;
            double y = pos.getY() - cam.y;
            double z = pos.getZ() - cam.z;

            // Draw filled box
            drawFilledBox(buffer, tessellator, x, y, z, 1.0, 1.0, 1.0,
                    fillRGBA[0], fillRGBA[1], fillRGBA[2], fillRGBA[3]);

            // Draw outline box
            drawOutlineBox(buffer, tessellator, x, y, z, 1.0, 1.0, 1.0,
                    outRGBA[0], outRGBA[1], outRGBA[2], outRGBA[3]);

            // Draw tracer line
            if (tracerLines.get()) {
                buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(0, mc.player.getEyeHeight() - (cam.y - mc.player.getPosY()), 0)
                        .color(outRGBA[0], outRGBA[1], outRGBA[2], outRGBA[3]).endVertex();
                buffer.pos(x + 0.5, y + 0.5, z + 0.5)
                        .color(outRGBA[0], outRGBA[1], outRGBA[2], outRGBA[3]).endVertex();
                tessellator.draw();
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void scanForDebris() {
        foundBlocks.clear();
        if (mc.player == null || mc.world == null) return;

        int radius = (int) searchRadius.get();
        BlockPos playerPos = mc.player.getPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (state.getBlock() == Blocks.ANCIENT_DEBRIS) {
                        foundBlocks.add(pos);
                    }
                    if (showNetheriteOre.get() && state.getBlock() == Blocks.NETHERITE_BLOCK) {
                        foundBlocks.add(pos);
                    }
                }
            }
        }
    }

    private void drawFilledBox(BufferBuilder buffer, Tessellator tess,
                               double x, double y, double z, double w, double h, double d,
                               float r, float g, float b, float a) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // Bottom face
        buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        // Top face
        buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        // Front
        buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        // Back
        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        // Left
        buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        // Right
        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        tess.draw();
    }

    private void drawOutlineBox(BufferBuilder buffer, Tessellator tess,
                                double x, double y, double z, double w, double h, double d,
                                float r, float g, float b, float a) {
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        // Bottom
        buffer.pos(x, y, z).color(r, g, b, a).endVertex(); buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex(); buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex(); buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex(); buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        // Top
        buffer.pos(x, y + h, z).color(r, g, b, a).endVertex(); buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex(); buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex(); buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex(); buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        // Verticals
        buffer.pos(x, y, z).color(r, g, b, a).endVertex(); buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex(); buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex(); buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex(); buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        tess.draw();
    }
}
