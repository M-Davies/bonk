package com.bonk;

import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;
import java.awt.event.*;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.security.SecureRandom;
import org.identityconnectors.common.security.GuardedString;

class AudioListener implements LineListener {
    private boolean done = false;

    @Override
    public synchronized void update(LineEvent event) {
        Type eventType = event.getType();
        if (eventType == Type.STOP || eventType == Type.CLOSE) {
            done = true;
            notifyAll();
        }
    }

    public synchronized void waitUntilDone() throws InterruptedException {
        while (!done) {
            wait();
        }
    }
}

public class Bonk {
    public static final char[] CTRL_CHARS = new char[]{'C', 't', 'r', 'l'};
    public static final char[] C_CHARS = new char[]{'C'};

    public Bonk() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                boolean ctrlPressed = false;

                public GuardedString getKeyText(NativeKeyEvent keyEvent) {
                    // Get and encrypt keypress code
                    char[] keypressChars = NativeKeyEvent.getKeyText(keyEvent.getKeyCode()).toCharArray();
                    GuardedString guardedChars = new GuardedString(keypressChars);
                    guardedChars.makeReadOnly();

                    // Wipe insecure char array in memory
                    SecureRandom sr = new SecureRandom();
                    for (int i = 0; i < keypressChars.length; i++)
                        keypressChars[i] = (char) sr.nextInt(Character.MAX_VALUE + 1);
                    keypressChars = null;

                    return guardedChars;
                }

                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                    GuardedString keyText = getKeyText(nativeEvent);        
                    keyText.access((char[] keyChars) -> {
                        if (ctrlPressed && Arrays.equals(keyChars, C_CHARS)) {
                            ctrlPressed = false;
                            playBonk();
                        }
                    });
                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                    GuardedString keyText = getKeyText(nativeEvent);
                    keyText.access((char[] keyChars) -> {
                        if (Arrays.equals(keyChars, CTRL_CHARS)) {
                            ctrlPressed = true;
                        }
                    });
                }
            });
            System.out.println("Ready to bonk some boys!");
        } catch (NativeHookException e) {
            System.err.println("Failed to setup keypress listiner. Do your permissions allowing for bonking?. Stacktrace:");
            e.printStackTrace();
        }
    }

    public static void playBonk() {
        try {
            System.out.println("Opening BONK...");
            File bonkFile = new File("src/main/resources/bonk.wav");
            System.out.println("Getting BONK stream...");
            InputStream bonkStream = new FileInputStream(bonkFile);
            InputStream bufferedBonkStream = new BufferedInputStream(bonkStream);
            System.out.println("Assigning BONK stream...");
            AudioListener listener = new AudioListener();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedBonkStream);
            try {
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(listener);
                clip.open(inputStream);
                try {
                    clip.start();
                    listener.waitUntilDone();
                    System.out.println("BONK!");
                } finally {
                    clip.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to play bonk sound. Please report this stacktrace to the author of the project:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Setting up service...");
        new Bonk();
    }
}
