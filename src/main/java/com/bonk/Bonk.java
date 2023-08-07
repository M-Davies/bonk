package com.bonk;

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
    public Bonk() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                boolean ctrlPressed = false;

                public String getKeyText(NativeEvent keyEvent) {
                    return NativeKeyEvent.getKeyText(keyEvent.getKeyCode());
                }

                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                    String keyText = getKeyText(nativeEvent);
                    System.out.println("User Released: " + keyText);
                    if (ctrlPressed && keyText == "C") {
                        ctrlPressed = false;
                        playBonk();
                    }
                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                    String keyText = getKeyText(nativeEvent);
                    System.out.println("User Pressed: " + keyText);
                    if (keyText == "Ctrl") {
                        ctrlPressed = true;
                    }
                }
            });
            System.out.println("Ready to bonk some boys!");
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    private static void playBonk() {
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
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Setting up service...");
        new Bonk();
    }
}
