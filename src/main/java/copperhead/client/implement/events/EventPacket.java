package copperhead.client.implement.events;

import copperhead.client.api.event.Event;
import net.minecraft.network.IPacket;

public class EventPacket extends Event {
    private IPacket<?> packet;
    private final boolean receive;

    public EventPacket(IPacket<?> packet, boolean receive) {
        this.packet = packet;
        this.receive = receive;
    }

    public IPacket<?> getPacket() { return packet; }
    public void setPacket(IPacket<?> packet) { this.packet = packet; }
    public boolean isReceive() { return receive; }
    public boolean isSend() { return !receive; }
}
