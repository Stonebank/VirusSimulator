package hk.sound;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager extends JFrame {

    public static boolean TICKED_OFF;

    private Clip clip;

    public SoundManager(String fileName) {
        if (!TICKED_OFF) {

            try {

                var url = new URL("file:./resources/" + fileName + ".wav");
                var audio = AudioSystem.getAudioInputStream(url);

                clip = AudioSystem.getClip();
                clip.open(audio);

                if (fileName.startsWith("ambulance")) {
                    FloatControl floatControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float db = (float) (Math.log(0.1) / Math.log(10) * 20);
                    floatControl.setValue(db);
                }
                clip.start();
                clip.loop((fileName.equalsIgnoreCase("plaque.wav") || fileName.equalsIgnoreCase("ambulance.wav") || fileName.startsWith("ambulance")) ? 4 : 0);

            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                System.err.println("ERROR! Something went wrong with the sound file: " + fileName + ": " + e);
                e.printStackTrace();
            }

        }
    }

    public void stop() {
        clip.stop();
        clip.flush();
    }

}
