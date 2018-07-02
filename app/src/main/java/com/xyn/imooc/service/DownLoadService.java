package com.xyn.imooc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.xyn.imooc.entry.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoadService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String DOWNLOADPATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/downloads/";
    public static final int MSG_INIT = 0;
    private DownLoadTask mTask = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d("xyn", "START :" + fileInfo.toString());
            new InitThread(fileInfo).start();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d("xyn", "STOP :" + fileInfo.toString());
            if (mTask != null) {
                mTask.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.d("xyn", "Init :" + fileInfo);
                    mTask = new DownLoadTask(DownLoadService.this, fileInfo);
                    mTask.download();
                    break;
            }
        }
    };

    /**
     * 初始化子线程
     */
    class InitThread extends Thread {
        private FileInfo mFileInfo = null;

        public InitThread(FileInfo fileInfo) {
            this.mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            super.run();
            try {
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOADPATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (Exception e) {

            } finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
