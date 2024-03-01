package com.opau.music;

import android.util.Log;

public class SoundFormatInfo {
    private String mimeType;
    private int sampleRate;
    private int bitrate;

    private boolean isLossless = false;

    public SoundFormatInfo(String mimeType, int sampleRate, int bitrate) {
        this.mimeType = mimeType.replace("audio/", "").toUpperCase();
        if (this.mimeType.equals("RAW")) {
            this.mimeType = "WAV";
        }
        this.sampleRate = sampleRate;
        this.bitrate = bitrate;
        isLossless = this.mimeType.equals("FLAC") || this.mimeType.equals("WAV") || this.mimeType.equals("ALAC");
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isLossless() {
        return isLossless;
    }

    public int getBitrate() {
        return bitrate;
    }
}
