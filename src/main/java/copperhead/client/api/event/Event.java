package copperhead.client.api.event;

public class Event {
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void setCancel(boolean cancel) {
        this.cancelled = cancel;
    }
}
