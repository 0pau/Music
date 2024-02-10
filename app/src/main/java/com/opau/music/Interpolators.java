package com.opau.music;

import android.view.animation.PathInterpolator;

public class Interpolators {

    public static final PathInterpolator easeOut = new PathInterpolator(0.690f, 0.915f, 0.000f, 1.000f);
    public static final PathInterpolator easeOutBounce = new PathInterpolator(0.000f, 0.205f, 0.000f, 1.245f);
    public static final PathInterpolator easeInOut = new PathInterpolator(0.560f, 0.005f, 0.200f, 1.000f);
    public static final PathInterpolator playbackPaneEaseIn = new PathInterpolator(1.000f, 0.005f, 1.000f, 0.980f);
    public static final PathInterpolator playbackPaneEaseOut = new PathInterpolator(0.000f, 0.050f, 0.000f, 1.000f);

}
