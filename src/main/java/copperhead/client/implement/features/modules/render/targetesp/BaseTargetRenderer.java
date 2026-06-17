package copperhead.client.implement.features.modules.render.targetesp;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.LivingEntity;

public abstract class BaseTargetRenderer {
    public abstract void render(MatrixStack matrixStack, LivingEntity target, float partialTicks);
    public void reset() {}
}
