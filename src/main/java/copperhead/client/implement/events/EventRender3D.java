package copperhead.client.implement.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import copperhead.client.api.event.Event;

public class EventRender3D extends Event {
    private final MatrixStack matrixStack;
    private final float partialTicks;

    public EventRender3D(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrixStack() { return matrixStack; }
    public float getPartialTicks() { return partialTicks; }
}
