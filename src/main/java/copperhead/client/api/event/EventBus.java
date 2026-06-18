package copperhead.client.api.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final Map<Class<?>, List<ListenerEntry>> listeners = new ConcurrentHashMap<>();

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                method.setAccessible(true);
                listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                        .add(new ListenerEntry(listener, method));
            }
        }
    }

    public void unregister(Object listener) {
        listeners.values().forEach(list -> list.removeIf(entry -> entry.instance == listener));
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> T post(T event) {
        List<ListenerEntry> entries = listeners.get(event.getClass());
        if (entries != null) {
            for (ListenerEntry entry : entries) {
                try {
                    entry.method.invoke(entry.instance, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return event;
    }

    private static class ListenerEntry {
        final Object instance;
        final Method method;

        ListenerEntry(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }
    }
}
