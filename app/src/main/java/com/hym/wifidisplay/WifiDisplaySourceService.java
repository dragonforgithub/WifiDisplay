package com.hym.wifidisplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.hym.rtplib.RemoteDisplay;

public class WifiDisplaySourceService extends Service {
    private static final String TAG = "WifiDisplaySourceService";
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private static final int REQUEST_CODE = 1;
    public static int resultCode;
    public static Intent resultData;
    public static Notification notification;
    public static Context context;
    private DisplayMetrics mDisplayMetrics;
    private String mIFace;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        //mMediaProjection = mProjectionManager.getMediaProjection(resultCode, resultData);

        String host = intent.getStringExtra(WfdConstants.SOURCE_HOST);
        int port = intent.getIntExtra(WfdConstants.SOURCE_PORT, -1);
        mIFace = host + ':' + port;
        Log.d(TAG, "onStartCommand mIFace : " + mIFace);

        // 设置不可删除
        /*
        CharSequence name = "Media Projection";
        String description = "This is a channel for media projection notifications.";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel("MUSIC_PLAYER_CHANNEL", name, importance);
        channel.setDescription(description);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

         */

        startMediaProjectionForeground();

        Intent projData = intent.getParcelableExtra(WfdConstants.PROJECTION_DATA);
        MediaProjectionManager mpm = getSystemService(MediaProjectionManager.class);
        mMediaProjection = mpm.getMediaProjection(Activity.RESULT_OK, projData);

        mDisplayMetrics = new DisplayMetrics();
        getSystemService(WindowManager.class).getDefaultDisplay().getRealMetrics(mDisplayMetrics);

        Log.d(TAG, "Start RemoteDisplay......");
        new RemoteDisplay(mMediaProjection, mDisplayMetrics, mIFace);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMediaProjectionForeground() {
        Log.d(TAG, "startMediaProjectionForeground......");
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, WifiP2pSettingsActivity.class); //点击后跳转的界面，可以设置跳转数据

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("is running......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }

        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }
    }

    /*
    private void startMediaProjectionForeground() {
        Log.d(TAG, "Start MediaProjectionForeground......");
        CharSequence name = "Media Projection";
        String description = "This is a channel for media projection notifications.";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel channel = new NotificationChannel("MUSIC_PLAYER_CHANNEL", name, importance);
        channel.setDescription(description);

        // 设置不可删除
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }

        Log.d(TAG, "Start RemoteDisplay......");
        new RemoteDisplay(mMediaProjection, mDisplayMetrics, mIFace);
    }
    */

}

/*
public class WifiDisplaySourceService extends Activity {
    private static final String TAG = "WifiDisplaySourceService";
    private MediaProjection mMediaProjection;
    private DisplayMetrics mDisplayMetrics;
    private String mIFace;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_display_source);
        Log.d(TAG, "onCreate");

        Intent intent = getIntent();
        Intent projData = intent.getParcelableExtra(WfdConstants.PROJECTION_DATA);
        MediaProjectionManager mpm = getSystemService(MediaProjectionManager.class);
        mMediaProjection = mpm.getMediaProjection(Activity.RESULT_OK, projData);
        String host = intent.getStringExtra(WfdConstants.SOURCE_HOST);
        int port = intent.getIntExtra(WfdConstants.SOURCE_PORT, -1);
        mIFace = host + ':' + port;
        mDisplayMetrics = new DisplayMetrics();
        getSystemService(WindowManager.class).getDefaultDisplay().getRealMetrics(mDisplayMetrics);

    }

    public void onClick(View v) {
        Log.d(TAG, "Start RemoteDisplay......");
        new RemoteDisplay(mMediaProjection, mDisplayMetrics, mIFace);
    }
}

 */