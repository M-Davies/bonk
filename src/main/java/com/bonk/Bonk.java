package com.bonk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;

/**
 * Hello world!
 *
 */

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
    public static void main(String[] args) {
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
                    System.out.println("Closing Clip...");
                }
            } finally {
                inputStream.close();
                System.out.println("Closing Stream...");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Done!");
    }
}
