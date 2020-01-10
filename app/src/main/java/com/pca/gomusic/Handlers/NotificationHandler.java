package com.pca.gomusic.Handlers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.RemoteViews;
import com.pca.gomusic.Activities.MainActivity;
import com.pca.gomusic.Classes.ListSongs;
import com.pca.gomusic.R;
import com.pca.gomusic.Services.PlayerService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.File;
import java.util.Objects;


public class NotificationHandler {

    private static final int NOTIFICATION_ID = 272448;
    private static final String CHANNEL_ID = "com.pca.gomusic.notification";
    private String CHANNEL_NAME = "CHANNEL";
    private Context context;
    private PlayerService service;
    private NotificationChannel notificationChannel;
    private boolean notificationActive;

    private Notification notificationCompat;
    private NotificationManager notificationManager;

    public NotificationHandler(Context context, PlayerService service) {
        this.context = context;
        this.service = service;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    private Notification.Builder createBuiderNotification(boolean removable) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        if (removable)
                return new Notification.Builder(context, CHANNEL_ID)
                        .setOngoing(false)
                        .setColorized(true)
                        .setStyle(new Notification.DecoratedMediaCustomViewStyle())
                        .setSmallIcon(R.drawable.ic_music)
                        .setContentIntent(contentIntent);
        else
                return new Notification.Builder(context, CHANNEL_ID)
                        .setOngoing(true)
                        .setColorized(true)
                        .setStyle(new Notification.DecoratedMediaCustomViewStyle())
                        .setSmallIcon(R.drawable.ic_music)
                        .setContentIntent(contentIntent);
            }else {
            if (removable)
                return new Notification.Builder(context)
                        .setOngoing(false)
                        .setSmallIcon(R.drawable.ic_music)
                        .setContentIntent(contentIntent);
        else
                return new Notification.Builder(context)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_music)
                        .setContentIntent(contentIntent);
            }
    }

    public void setNotificationPlayer(boolean removable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        notificationCompat = createBuiderNotification(removable).build();
        RemoteViews notiLayoutBig = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        RemoteViews notiCollapsedView = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationCompat.bigContentView = notiLayoutBig;
        notificationCompat.contentView = notiCollapsedView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationCompat.color = Color.BLACK;
        }
        notificationCompat.priority = Notification.PRIORITY_MAX;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        if (!removable) {
            service.startForeground(NOTIFICATION_ID, notificationCompat);
            Objects.requireNonNull(notificationManager).notify(NOTIFICATION_ID, notificationCompat);
            notificationActive = true;
        }else {
            notificationActive = false;
        }
    }


    private void createChannel(){
        // create android channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setShowBadge(true);
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }
    }


    public void changeNotificationDetails(String songName, String artistName, long albumId, boolean playing) {

        if (artistName.equals("<unknown>")){
            artistName = context.getResources().getString(R.string.unkown_artist);
        }

        notificationCompat.bigContentView.setTextViewText(R.id.noti_name, songName);
        notificationCompat.bigContentView.setTextViewText(R.id.noti_artist, artistName);

        notificationCompat.contentView.setTextViewText(R.id.noti_name, songName);
        notificationCompat.contentView.setTextViewText(R.id.noti_artist, artistName);

        Intent playClick = new Intent();
        playClick.setAction(PlayerService.ACTION_PAUSE_SONG);
        PendingIntent playClickIntent = PendingIntent.getBroadcast(context, 21021, playClick, 0);
        notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);
        notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);

        Intent prevClick = new Intent();
        prevClick.setAction(PlayerService.ACTION_PREV_SONG);
        PendingIntent prevClickIntent = PendingIntent.getBroadcast(context, 21121, prevClick, 0);
        notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);
        notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);

        Intent nextClick = new Intent();
        nextClick.setAction(PlayerService.ACTION_NEXT_SONG);
        PendingIntent nextClickIntent = PendingIntent.getBroadcast(context, 21221, nextClick, 0);
        notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);
        notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);

        int playStateRes;
        int nextRes;
        int prevRes;
        if (playing) {
            playStateRes = R.drawable.ic_pause_white;
            nextRes = R.drawable.ic_next_white;
            prevRes = R.drawable.ic_previous_white;
        }else {
            playStateRes = R.drawable.ic_play_white;
            nextRes = R.drawable.ic_next_white;
            prevRes = R.drawable.ic_previous_white;
        }

        notificationCompat.bigContentView.setImageViewResource(R.id.noti_play_button, playStateRes);
        notificationCompat.contentView.setImageViewResource(R.id.noti_play_button, playStateRes);

        notificationCompat.bigContentView.setImageViewResource(R.id.noti_next_button, nextRes);

        notificationCompat.bigContentView.setImageViewResource(R.id.noti_prev_button, prevRes);

        String path = ListSongs.INSTANCE.getAlbumArt(context, albumId);
        if (!path.matches("")) {
            Picasso.with(context).load(new File(path)).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
//                    Palette.from(bitmap).generate(palette -> {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            notificationCompat.color = Objects.requireNonNull(palette).getDarkVibrantColor(0xffffffff);
//
//                            int[] colors = new Utils(context).getAvailableColor(palette);
//                            notificationCompat.bigContentView.setTextColor(R.id.noti_name, colors[1]);
//                            notificationCompat.contentView.setTextColor(R.id.noti_name, colors[1]);
//
//                            notificationCompat.bigContentView.setTextColor(R.id.noti_artist, colors[2]);
//                            notificationCompat.contentView.setTextColor(R.id.noti_artist, colors[2]);
//                        }
//                    });
                    notificationManager.notify(NOTIFICATION_ID, notificationCompat);
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) { }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) { }
            });
        } else {
            setDefaultImageView();
        }
    }

    private void setDefaultImageView() {
        notificationCompat.bigContentView.setImageViewResource(R.id.noti_album_art, R.drawable._art000);
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public void updateNotificationView() {
        setNotificationPlayer(false);
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public boolean isNotificationActive() {
        return notificationActive;
    }

    public void setNotificationActive(boolean notificationActive) {
        this.notificationActive = notificationActive;
    }

    public void dismissNotification(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

}
