package copperhead.client.api.feature.setting;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    protected T value;
    private BooleanSupplier visible = () -> true;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T get() { return value; }
    public T getValue() { return value; }
    public void set(T value) { this.value = value; }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S setVisible(BooleanSupplier visible) {
        this.visible = visible;
        return (S) this;
    }

    public boolean isVisible() { return visible.getAsBoolean(); }
}
