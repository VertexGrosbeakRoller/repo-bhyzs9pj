package copperhead.client.implement.features.modules.misc;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.api.feature.setting.StringSetting;
import copperhead.client.common.util.math.TimerUtils;
import copperhead.client.implement.events.EventPacket;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ChatType;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerHelper extends Module {

    public ModeSetting mode = new ModeSetting("Режим", "FunTime",
            "FunTime", "HolyWorld", "ReallyWorld", "LonyGrief");

    // FunTime
    private final BooleanSetting ftAutoAuth = new BooleanSetting("Авто авторизация", false)
            .setVisible(() -> mode.is("FunTime"));
    private final StringSetting ftPassword = new StringSetting("Пароль FT", "")
            .setVisible(() -> mode.is("FunTime") && ftAutoAuth.get());
    private final BooleanSetting ftAutoTPA = new BooleanSetting("Авто /tpaccept", false)
            .setVisible(() -> mode.is("FunTime"));
    private final BooleanSetting ftAutoGG = new BooleanSetting("Авто GG", false)
            .setVisible(() -> mode.is("FunTime"));

    // HolyWorld
    private final BooleanSetting hwAutoAuth = new BooleanSetting("Авто авторизация", false)
            .setVisible(() -> mode.is("HolyWorld"));
    private final StringSetting hwPassword = new StringSetting("Пароль HW", "")
            .setVisible(() -> mode.is("HolyWorld") && hwAutoAuth.get());
    private final BooleanSetting hwAutoReport = new BooleanSetting("Авто /report при дамаге", false)
            .setVisible(() -> mode.is("HolyWorld"));

    // ReallyWorld
    private final BooleanSetting rwAutoAuth = new BooleanSetting("Авто авторизация", false)
            .setVisible(() -> mode.is("ReallyWorld"));
    private final StringSetting rwPassword = new StringSetting("Пароль RW", "")
            .setVisible(() -> mode.is("ReallyWorld") && rwAutoAuth.get());
    private final BooleanSetting rwAutoTPA = new BooleanSetting("Авто /tpaccept", false)
            .setVisible(() -> mode.is("ReallyWorld"));
    private final BooleanSetting rwAutoEnchant = new BooleanSetting("Авто /enchant", false)
            .setVisible(() -> mode.is("ReallyWorld"));

    // LonyGrief
    private final BooleanSetting lgAutoAuth = new BooleanSetting("Авто авторизация", false)
            .setVisible(() -> mode.is("LonyGrief"));
    private final StringSetting lgPassword = new StringSetting("Пароль LG", "")
            .setVisible(() -> mode.is("LonyGrief") && lgAutoAuth.get());
    private final BooleanSetting lgAutoGG = new BooleanSetting("Авто GG", false)
            .setVisible(() -> mode.is("LonyGrief"));

    // General
    private final SliderSetting commandDelay = new SliderSetting("Задержка команд (мс)", 1000f, 100f, 5000f, 100f);

    private final TimerUtils timer = new TimerUtils();
    private final Queue<String> commandQueue = new ArrayDeque<>();
    private boolean authenticated = false;

    private static final Pattern AUTH_PATTERN = Pattern.compile("/(?:login|register|l|reg)\\s+.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern TPA_PATTERN = Pattern.compile("(?:.*просит телепортироваться.*|.*wants to teleport.*|.*tpa request.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KILL_PATTERN = Pattern.compile("(?:.*убил.*|.*killed.*|.*won the game.*|.*выиграл.*)", Pattern.CASE_INSENSITIVE);

    public ServerHelper() {
        super("ServerHelper", "Server Helper", ModuleCategory.MISC);
        addSettings(mode, commandDelay,
                ftAutoAuth, ftPassword, ftAutoTPA, ftAutoGG,
                hwAutoAuth, hwPassword, hwAutoReport,
                rwAutoAuth, rwPassword, rwAutoTPA, rwAutoEnchant,
                lgAutoAuth, lgPassword, lgAutoGG);
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.player.connection == null) return;

        // Process command queue
        if (!commandQueue.isEmpty() && timer.hasReached((long) commandDelay.get())) {
            String cmd = commandQueue.poll();
            if (cmd != null) {
                mc.player.sendChatMessage(cmd);
                timer.reset();
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket event) {
        if (mc.player == null) return;

        if (event.isReceive() && event.getPacket() instanceof SChatPacket) {
            SChatPacket chatPacket = (SChatPacket) event.getPacket();
            if (chatPacket.getType() == ChatType.GAME_INFO) return;

            String message = chatPacket.getChatComponent().getString();
            handleChatMessage(message);
        }
    }

    private void handleChatMessage(String message) {
        String lowerMsg = message.toLowerCase();

        // Auto auth
        if (!authenticated && (lowerMsg.contains("/login") || lowerMsg.contains("/register")
                || lowerMsg.contains("/l ") || lowerMsg.contains("/reg "))) {
            String password = getPassword();
            if (!password.isEmpty()) {
                if (lowerMsg.contains("/register") || lowerMsg.contains("/reg")) {
                    queueCommand("/register " + password + " " + password);
                } else {
                    queueCommand("/login " + password);
                }
                authenticated = true;
            }
        }

        // Auto TPA
        boolean tpaEnabled = mode.is("FunTime") ? ftAutoTPA.get()
                : mode.is("ReallyWorld") ? rwAutoTPA.get() : false;
        if (tpaEnabled && TPA_PATTERN.matcher(message).find()) {
            queueCommand("/tpaccept");
        }

        // Auto GG
        boolean ggEnabled = mode.is("FunTime") ? ftAutoGG.get()
                : mode.is("LonyGrief") ? lgAutoGG.get() : false;
        if (ggEnabled && KILL_PATTERN.matcher(message).find()) {
            queueCommand("GG");
        }
    }

    private String getPassword() {
        switch (mode.get()) {
            case "FunTime": return ftPassword.get();
            case "HolyWorld": return hwPassword.get();
            case "ReallyWorld": return rwPassword.get();
            case "LonyGrief": return lgPassword.get();
            default: return "";
        }
    }

    private boolean isAutoAuthEnabled() {
        switch (mode.get()) {
            case "FunTime": return ftAutoAuth.get();
            case "HolyWorld": return hwAutoAuth.get();
            case "ReallyWorld": return rwAutoAuth.get();
            case "LonyGrief": return lgAutoAuth.get();
            default: return false;
        }
    }

    private void queueCommand(String command) {
        commandQueue.add(command);
    }

    @Override
    public boolean onEnable() {
        authenticated = false;
        commandQueue.clear();
        timer.reset();
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        commandQueue.clear();
        authenticated = false;
        super.onDisable();
    }
}
