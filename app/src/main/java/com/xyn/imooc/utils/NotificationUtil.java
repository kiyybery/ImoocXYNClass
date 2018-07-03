package com.xyn.imooc.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

import com.xyn.imooc.MainActivity;
import com.xyn.imooc.R;
import com.xyn.imooc.entry.FileInfo;
import com.xyn.imooc.service.DownLoadService;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtil {
    private NotificationManager mNotificationManager = null;

    private Map<Integer, Notification> mNotifications = null;
    private Context mContext = null;

    public NotificationUtil(Context context) {
        mContext = context;
        mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        mNotifications = new HashMap<Integer, Notification>();
    }

    public void showNotification(FileInfo fileInfo) {
        if (!mNotifications.containsKey(fileInfo.getId())) {
            Notification notification = new Notification();
            notification.tickerText = fileInfo.getFileName() + "开始下载";
            notification.when = System.currentTimeMillis();
            notification.icon = R.drawable.ic_launcher_background;
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notification.contentIntent = pendingIntent;

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification);

            Intent intentStart = new Intent(mContext, DownLoadService.class);
            intentStart.setAction(DownLoadService.ACTION_START);
            intentStart.putExtra("fileInfo", fileInfo);
            PendingIntent piStart = PendingIntent.getService(mContext, 0, intentStart, 0);
            remoteViews.setOnClickPendingIntent(R.id.btStart, piStart);

            Intent intentStop = new Intent(mContext, DownLoadService.class);
            intentStop.setAction(DownLoadService.ACTION_STOP);
            intentStop.putExtra("fileInfo", fileInfo);
            PendingIntent piStop = PendingIntent.getService(mContext, 0, intentStop, 0);
            remoteViews.setOnClickPendingIntent(R.id.btStop, piStop);

            remoteViews.setTextViewText(R.id.tvfileName, fileInfo.getFileName());
            notification.contentView = remoteViews;

            mNotificationManager.notify(fileInfo.getId(), notification);
            mNotifications.put(fileInfo.getId(), notification);
        }
    }

    public void cancelNotification(int id) {
        mNotificationManager.cancel(id);
        mNotifications.remove(id);
    }

    public void updateNotification(int id, int progress) {
        Notification notification = mNotifications.get(id);
        if (notification != null) {
            notification.contentView.setProgressBar(R.id.pbProgress, 100, progress, false);
            mNotificationManager.notify(id, notification);
        }
    }
}
