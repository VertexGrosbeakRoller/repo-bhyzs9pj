package copperhead.client.common.util.math;

public class AnimationUtil {
    private float value;
    private float speed;
    private float target;

    public AnimationUtil(float value, float speed, Object easing) {
        this.value = value;
        this.speed = speed;
        this.target = value;
    }

    public void update(float target) {
        this.target = target;
        value += (target - value) * speed;
    }

    public float getValue() {
        return value;
    }

    public void reset() {
        value = 0;
        target = 0;
    }
}
