package copperhead.client.implement.events;

import copperhead.client.api.event.Event;

public class EventMotion extends Event {
    private float yaw, pitch;

    public EventMotion(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }
}
