package copperhead.client.api.feature.setting;

import java.util.Arrays;
import java.util.List;

public class MultiBooleanSetting extends Setting<List<BooleanSetting>> {
    public MultiBooleanSetting(String name, BooleanSetting... settings) {
        super(name, Arrays.asList(settings));
    }

    public BooleanSetting get(int index) {
        return getValue().get(index);
    }

    public boolean is(String name) {
        return getValue().stream().anyMatch(s -> s.getName().equals(name) && s.get());
    }

    public BooleanSetting getValueByName(String name) {
        return getValue().stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }
}
