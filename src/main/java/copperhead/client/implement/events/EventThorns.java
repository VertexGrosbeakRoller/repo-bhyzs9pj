package copperhead.client.implement.events;

import copperhead.client.api.event.Event;
import net.minecraft.entity.Entity;

public class EventThorns extends Event {
    private final Entity attacker;

    public EventThorns(Entity attacker) {
        this.attacker = attacker;
    }

    public Entity getAttacker() { return attacker; }
}
