package copperhead.client.api.feature.setting;

public class SliderSetting extends Setting<Float> {
    private final float min, max, step;

    public SliderSetting(String name, float defaultValue, float min, float max, float step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
    public float getStep() { return step; }

    @Override
    public void set(Float value) {
        super.set(Math.min(max, Math.max(min, value)));
    }
}
