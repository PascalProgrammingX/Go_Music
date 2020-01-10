package com.pca.gomusic.Handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import static io.fabric.sdk.android.Fabric.TAG;


public class UserPreferenceHandler {

    public static final String PREF_NAME = "com.pca.gomusic";
    private static final String REPEAT_ALL = "repeat_all";
    private static final String REPEAT_ONE = "repeat_one";
    private static final String SHUFFLE = "shuffle";
    private static final String pause = "pause";
    public static final String PLAYING = "isPlaying";
    public static final String SONG_POSITION = "pos";
    private final SharedPreferences shp;

    public UserPreferenceHandler(Context context) {
        shp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public void setRepeatAllEnable(boolean enable) {
        shp.edit().putBoolean(REPEAT_ALL, enable).apply();
        Log.d(TAG," REPEAT_ALL = " + enable);
    }

    public void setRepeatOneEnable(boolean enable) {
        shp.edit().putBoolean(REPEAT_ONE, enable).apply();
        Log.d(TAG," REPEAT_ONE = " + enable);
    }


    public void setPause(boolean enabled){
        shp.edit().putBoolean(pause, enabled).apply();
        Log.d(TAG," PAUSED = " + enabled);
    }


    public void setRepeatEnable() {
        if (isRepeatAllEnabled()) {
            setRepeatAllEnable(false);
            setRepeatOneEnable(true);
        } else if (isRepeatOneEnabled()) {
            setRepeatOneEnable(false);
        } else {
            setRepeatAllEnable(true);
        }
    }

    public boolean isRepeatAllEnabled() {
        return shp.getBoolean(REPEAT_ALL, false);
    }

    public boolean isRepeatOneEnabled() {
        return shp.getBoolean(REPEAT_ONE, false);
    }


    public boolean isPaused() {
        return shp.getBoolean(pause, false);
    }


    public boolean isShuffleEnabled() {
        return shp.getBoolean(SHUFFLE, false);
    }

    public void setShuffleEnabled(boolean value) {
        shp.edit().putBoolean(SHUFFLE, value).apply();
        Log.d(TAG," SHUFFLE = " + value );
    }

}