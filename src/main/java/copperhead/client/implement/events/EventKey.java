package copperhead.client.implement.events;

import copperhead.client.api.event.Event;

public class EventKey extends Event {
    private final int key;

    public EventKey(int key) {
        this.key = key;
    }

    public int getKey() { return key; }
}
