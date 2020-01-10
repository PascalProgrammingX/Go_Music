package com.pca.gomusic.Activities;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.annotation.RequiresApi;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.pca.gomusic.R;


public class VideoPlayerActivity extends Activity{


  private VideoView vv;
  private AudioManager audioManager;
  private InterstitialAd interstitialAd;

  public static final String VIDEO_DATA = "VIDEO_DATA";

  private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
        if (vv != null && vv.isPlaying()) {
          vv.pause();
        }
      }
      if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        vv.resume();
      }
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS && vv.isPlaying()) {
        audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
      }
    }
  };



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_activity);
    vv = findViewById(R.id.video);

    interstitialAd = new InterstitialAd(this);
    interstitialAd.setAdUnitId(getResources().getString(R.string.video_interstial));
    interstitialAd.loadAd(new AdRequest.Builder().build());

    //Keep screen on while video plays.
    getWindow().getDecorView().setKeepScreenOn(true);

    audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
    assert audioManager != null;
    audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    playVideo();

    new Handler().postDelayed(() -> {
      if (interstitialAd.isLoaded()){
        interstitialAd.show();
      }
    }, 180000);

  }


  @Override
  public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
    if (isInPictureInPictureMode){
      newConfig.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
    }  super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
  }





  @Override
  public void onUserLeaveHint(){
    super.onUserLeaveHint();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      PictureInPictureParams params = new PictureInPictureParams.Builder()
              .setAspectRatio(getPipRatio())
              .build();
      enterPictureInPictureMode(params);
    }
  }




  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    playVideo();
  }



  private void playVideo(){
    if (vv.isPlaying()){
      vv.stopPlayback();
    }
    Intent intent = getIntent();
    String videoData = intent.getStringExtra(VIDEO_DATA);
    vv.setVideoURI(Uri.parse(videoData));
    vv.setMediaController(new MediaController(this));
    vv.start();
    vv.setKeepScreenOn(true);
  }






  @Override
  public void onBackPressed() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      PictureInPictureParams params = new PictureInPictureParams.Builder()
              .setAspectRatio(getPipRatio())
              .build();
      enterPictureInPictureMode(params);
    }else {
      finish();
    }
    if (interstitialAd.isLoaded()){
      new Handler().postDelayed(() -> interstitialAd.show(), 2000);
    }
    super.onBackPressed();
  }


  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public  Rational getPipRatio(){
    //Fixed differences in video width and height for PIP mode.
    int width = vv.getWidth();
    int height =  vv.getHeight();
    return new Rational(width, height);
  }

}