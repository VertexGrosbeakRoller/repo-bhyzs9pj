package copperhead.client.implement.events;

import copperhead.client.api.event.Event;
import net.minecraft.entity.Entity;

public class EventDamage extends Event {
    private final Entity attacker;
    private final Entity target;
    private final float amount;

    public EventDamage(Entity attacker, Entity target, float amount) {
        this.attacker = attacker;
        this.target = target;
        this.amount = amount;
    }

    public Entity getAttacker() { return attacker; }
    public Entity getTarget() { return target; }
    public float getAmount() { return amount; }
}
