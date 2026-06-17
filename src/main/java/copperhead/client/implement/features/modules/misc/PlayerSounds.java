package copperhead.client.implement.features.modules.misc;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ButtonSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.implement.events.EventUpdate;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PlayerSounds — воспроизводит пользовательские звуки из папки.
 * При открытии папки пользователь закидывает .wav файлы,
 * которые проигрываются по очереди (очередь/цикл).
 */
public class PlayerSounds extends Module {

    private final ButtonSetting openFolder = new ButtonSetting("Папка", "Открыть")
            .addButton("Открыть", this::openSoundsFolder);
    private final SliderSetting volume = new SliderSetting("Громкость", 0.5f, 0.0f, 1.0f, 0.05f);
    private final SliderSetting intervalSec = new SliderSetting("Интервал (сек)", 5f, 0.5f, 30f, 0.5f);
    private final BooleanSetting shuffle = new BooleanSetting("Перемешать", false);
    private final BooleanSetting loop = new BooleanSetting("Зацикливать", true);
    private final BooleanSetting onKillOnly = new BooleanSetting("Только при килле", false);

    private final List<File> soundFiles = new ArrayList<>();
    private int currentIndex = 0;
    private long lastPlayTime = 0;
    private Clip currentClip;
    private File soundsDirectory;

    public PlayerSounds() {
        super("PlayerSounds", "Player Sounds", ModuleCategory.MISC);
        addSettings(openFolder, volume, intervalSec, shuffle, loop, onKillOnly);

        soundsDirectory = new File(System.getProperty("user.home"), "CopperHead/sounds");
        if (!soundsDirectory.exists()) {
            soundsDirectory.mkdirs();
        }
    }

    @Override
    public boolean onEnable() {
        loadSounds();
        currentIndex = 0;
        lastPlayTime = 0;
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        stopCurrentSound();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || soundFiles.isEmpty()) return;
        if (onKillOnly.get()) return; // Kill detection handled separately

        long now = System.currentTimeMillis();
        if (now - lastPlayTime < intervalSec.get() * 1000) return;

        playNextSound();
        lastPlayTime = now;
    }

    public void onKill() {
        if (onKillOnly.get() && !soundFiles.isEmpty()) {
            playNextSound();
        }
    }

    private void playNextSound() {
        if (soundFiles.isEmpty()) return;

        if (currentIndex >= soundFiles.size()) {
            if (loop.get()) {
                currentIndex = 0;
                if (shuffle.get()) Collections.shuffle(soundFiles);
            } else {
                return;
            }
        }

        File file = soundFiles.get(currentIndex);
        currentIndex++;

        stopCurrentSound();
        playSound(file);
    }

    private void playSound(File file) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioIn);

            // Set volume
            if (currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log10(Math.max(0.001, volume.get())) * 20.0);
                gainControl.setValue(dB);
            }

            currentClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    private void stopCurrentSound() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
        }
        currentClip = null;
    }

    private void loadSounds() {
        soundFiles.clear();
        if (soundsDirectory == null || !soundsDirectory.exists()) return;

        File[] files = soundsDirectory.listFiles((dir, name) ->
                name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3"));
        if (files != null) {
            Collections.addAll(soundFiles, files);
            if (shuffle.get()) Collections.shuffle(soundFiles);
        }
    }

    private void openSoundsFolder() {
        if (soundsDirectory == null) return;
        if (!soundsDirectory.exists()) soundsDirectory.mkdirs();
        try {
            Desktop.getDesktop().open(soundsDirectory);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
