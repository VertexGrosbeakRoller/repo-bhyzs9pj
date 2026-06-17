package copperhead.client.common.util.math;

public class TimerUtils {
    private long lastTime = System.currentTimeMillis();

    public boolean hasReached(long millis) {
        return System.currentTimeMillis() - lastTime >= millis;
    }

    public void reset() {
        lastTime = System.currentTimeMillis();
    }

    public long getElapsed() {
        return System.currentTimeMillis() - lastTime;
    }
}
