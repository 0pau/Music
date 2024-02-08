package com.opau.music;

import java.io.IOException;

public class PlaybackCoordinatorEventListener {

    void onTrackStarted(long track_id) {};
    void onPlayBackError(){};
    void onPlayListFinished(){};
    void onPlayStateChanged(boolean isPlaying){};
}
