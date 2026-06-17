package copperhead.client.implement.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import copperhead.client.api.event.Event;

public class EventRender2D extends Event {
    private final MatrixStack matrixStack;
    private final float partialTicks;

    public EventRender2D(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrixStack() { return matrixStack; }
    public float getPartialTicks() { return partialTicks; }

    public static class Pre extends EventRender2D {
        public Pre(MatrixStack matrixStack, float partialTicks) {
            super(matrixStack, partialTicks);
        }
    }

    public static class Post extends EventRender2D {
        public Post(MatrixStack matrixStack, float partialTicks) {
            super(matrixStack, partialTicks);
        }
    }
}
