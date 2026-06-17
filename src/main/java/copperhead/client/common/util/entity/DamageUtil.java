package copperhead.client.common.util.entity;

import copperhead.client.implement.events.EventDamage;
import copperhead.client.implement.events.EventPacket;

public class DamageUtil {
    private long lastDamageTime;
    private long maxTime = 700;
    private boolean normalDamage;

    public void time(long ms) {
        this.maxTime = ms;
    }

    public void processDamage(EventDamage e) {
        lastDamageTime = System.currentTimeMillis();
        normalDamage = true;
    }

    public void onPacketEvent(EventPacket e) {
        // process packet for damage detection
    }

    public boolean isNormalDamage() {
        if (System.currentTimeMillis() - lastDamageTime > maxTime) {
            normalDamage = false;
        }
        return normalDamage;
    }
}
