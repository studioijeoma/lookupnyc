package com.ekeneijeoma.lookup;

import processing.android.PWallpaper;
import processing.core.PApplet;

public class MainWallpaper extends PWallpaper {
    String TAG = "MainWallpaper";

    @Override
    public PApplet createSketch() {
        return new MainPApplet();
    }
}
