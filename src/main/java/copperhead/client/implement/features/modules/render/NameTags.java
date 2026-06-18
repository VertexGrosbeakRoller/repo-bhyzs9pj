package copperhead.client.implement.features.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.math.MathUtils;
import copperhead.client.common.util.render.ColorUtils;
import copperhead.client.implement.events.EventRender3D;
import copperhead.client.managers.FriendManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.text.TextFormatting;

public class NameTags extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Стандартный", "Стандартный", "Расширенный");
    private final BooleanSetting showHealth = new BooleanSetting("Показать HP", true);
    private final BooleanSetting showArmor = new BooleanSetting("Показать броню", true);
    private final BooleanSetting showDistance = new BooleanSetting("Показать дистанцию", true);
    private final BooleanSetting showPing = new BooleanSetting("Показать пинг", false);
    private final BooleanSetting friendHighlight = new BooleanSetting("Подсветка друзей", true);
    private final SliderSetting scaleValue = new SliderSetting("Размер", 1.0f, 0.3f, 3.0f, 0.1f);
    private final BooleanSetting background = new BooleanSetting("Фон", true);

    public NameTags() {
        super("NameTags", "Name Tags", ModuleCategory.RENDER);
        addSettings(mode, showHealth, showArmor, showDistance, showPing, friendHighlight, scaleValue, background);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.level == null) return;

        float partialTicks = event.getPartialTicks();

        for (PlayerEntity player : mc.level.players()) {
            if (player == mc.player || !player.isAlive()) continue;
            if (player.isInvisible()) continue;

            if (mode.is("Стандартный")) {
                renderStandard(event.getMatrixStack(), player, partialTicks);
            } else {
                renderExtended(event.getMatrixStack(), player, partialTicks);
            }
        }
    }

    private void renderStandard(MatrixStack matrixStack, PlayerEntity player, float partialTicks) {
        double x = MathUtils.interpolate(player.getX(), player.xo, partialTicks);
        double y = MathUtils.interpolate(player.getY(), player.yo, partialTicks);
        double z = MathUtils.interpolate(player.getZ(), player.zo, partialTicks);
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();

        double renderX = x - cam.x;
        double renderY = y - cam.y + player.getBbHeight() + 0.5;
        double renderZ = z - cam.z;

        String name = player.getName().getString();
        boolean isFriend = FriendManager.isFriend(name);

        StringBuilder text = new StringBuilder();
        if (isFriend && friendHighlight.get()) {
            text.append(TextFormatting.GREEN);
        }
        text.append(name);

        if (showHealth.get()) {
            float health = player.getHealth();
            String healthColor;
            if (health > 14) healthColor = TextFormatting.GREEN.toString();
            else if (health > 7) healthColor = TextFormatting.YELLOW.toString();
            else healthColor = TextFormatting.RED.toString();
            text.append(" ").append(healthColor).append(String.format("%.1f", health)).append("HP");
        }

        if (showDistance.get()) {
            float dist = mc.player.distanceTo(player);
            text.append(TextFormatting.GRAY).append(" [").append(String.format("%.1f", dist)).append("m]");
        }

        FontRenderer font = mc.font;
        float scale = scaleValue.get() * 0.025f;
        float distance = (float) mc.player.distanceTo(player);
        scale = Math.max(scale, scale * distance / 10.0f);

        matrixStack.pushPose();
        matrixStack.translate(renderX, renderY, renderZ);
        matrixStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        matrixStack.scale(-scale, -scale, scale);

        String finalText = text.toString();
        float textWidth = font.width(finalText);
        float xOffset = -textWidth / 2.0f;



        Matrix4f matrix = matrixStack.last().pose();
        font.drawInBatch(finalText, xOffset, 0, 0xFFFFFFFF, false, matrix,
                mc.renderBuffers().bufferSource(), true, background.get() ? 0x80000000 : 0, 15728880);
        mc.renderBuffers().bufferSource().endBatch();

        matrixStack.popPose();
    }

    private void renderExtended(MatrixStack matrixStack, PlayerEntity player, float partialTicks) {
        double x = MathUtils.interpolate(player.getX(), player.xo, partialTicks);
        double y = MathUtils.interpolate(player.getY(), player.yo, partialTicks);
        double z = MathUtils.interpolate(player.getZ(), player.zo, partialTicks);
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();

        double renderX = x - cam.x;
        double renderY = y - cam.y + player.getBbHeight() + 0.5;
        double renderZ = z - cam.z;

        String name = player.getName().getString();
        boolean isFriend = FriendManager.isFriend(name);

        StringBuilder line1 = new StringBuilder();
        if (isFriend && friendHighlight.get()) {
            line1.append(TextFormatting.GREEN).append("[F] ");
        }
        line1.append(TextFormatting.WHITE).append(name);

        StringBuilder line2 = new StringBuilder();
        if (showHealth.get()) {
            float health = player.getHealth();
            float absorption = player.getAbsorptionAmount();
            String healthColor;
            if (health > 14) healthColor = TextFormatting.GREEN.toString();
            else if (health > 7) healthColor = TextFormatting.YELLOW.toString();
            else healthColor = TextFormatting.RED.toString();
            line2.append(healthColor).append(String.format("%.1fHP", health));
            if (absorption > 0) {
                line2.append(TextFormatting.GOLD).append(String.format(" +%.1f", absorption));
            }
        }

        if (showDistance.get()) {
            float dist = mc.player.distanceTo(player);
            line2.append(TextFormatting.GRAY).append(String.format(" %.1fm", dist));
        }

        if (showPing.get() && mc.getConnection() != null) {
            NetworkPlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
            if (info != null) {
                int ping = info.getLatency();
                String pingColor;
                if (ping < 100) pingColor = TextFormatting.GREEN.toString();
                else if (ping < 200) pingColor = TextFormatting.YELLOW.toString();
                else pingColor = TextFormatting.RED.toString();
                line2.append(pingColor).append(String.format(" %dms", ping));
            }
        }

        FontRenderer font = mc.font;
        float scale = scaleValue.get() * 0.025f;
        float distance = (float) mc.player.distanceTo(player);
        scale = Math.max(scale, scale * distance / 10.0f);

        matrixStack.pushPose();
        matrixStack.translate(renderX, renderY, renderZ);
        matrixStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        matrixStack.scale(-scale, -scale, scale);

        Matrix4f matrix = matrixStack.last().pose();
        String finalLine1 = line1.toString();
        String finalLine2 = line2.toString();

        float w1 = font.width(finalLine1);
        float w2 = font.width(finalLine2);

        font.drawInBatch(finalLine1, -w1 / 2, -10, 0xFFFFFFFF, false, matrix,
                mc.renderBuffers().bufferSource(), true, background.get() ? 0x80000000 : 0, 15728880);
        font.drawInBatch(finalLine2, -w2 / 2, 0, 0xFFFFFFFF, false, matrix,
                mc.renderBuffers().bufferSource(), true, background.get() ? 0x80000000 : 0, 15728880);
        mc.renderBuffers().bufferSource().endBatch();

        // Render armor icons if enabled
        if (showArmor.get()) {
            renderArmorRow(matrixStack, player, matrix);
        }

        matrixStack.popPose();
    }

    private void renderArmorRow(MatrixStack matrixStack, PlayerEntity player, Matrix4f matrix) {
        // Render armor items above the name
        ItemStack[] armor = new ItemStack[]{
                player.inventory.armor.get(3),
                player.inventory.armor.get(2),
                player.inventory.armor.get(1),
                player.inventory.armor.get(0),
                player.getMainHandItem(),
                player.getOffhandItem()
        };
        // Armor rendering requires item renderer which is complex in 3D space
        // This is handled via the 2D overlay render in practice
    }
}
