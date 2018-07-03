package com.xyn.imooc.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.xyn.imooc.MainActivity;
import com.xyn.imooc.R;
import com.xyn.imooc.entry.FileInfo;

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
        }
    }
}
