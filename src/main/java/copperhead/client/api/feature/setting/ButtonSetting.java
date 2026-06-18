package copperhead.client.api.feature.setting;

import java.util.LinkedHashMap;
import java.util.Map;

public class ButtonSetting extends Setting<String> {
    private final Map<String, Runnable> buttons = new LinkedHashMap<>();

    public ButtonSetting(String name, String defaultLabel) {
        super(name, defaultLabel);
    }

    public ButtonSetting addButton(String label, Runnable action) {
        buttons.put(label, action);
        return this;
    }

    public void press(String label) {
        Runnable r = buttons.get(label);
        if (r != null) r.run();
    }

    public Map<String, Runnable> getButtons() { return buttons; }
}
