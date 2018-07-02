package com.xyn.imooc.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xyn.imooc.db.ThreadDAO;
import com.xyn.imooc.db.ThreadDAOImpl;
import com.xyn.imooc.entry.FileInfo;
import com.xyn.imooc.entry.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class DownLoadTask {
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDao = null;
    private int mFinished = 0;
    public boolean isPause = false;

    public DownLoadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContext);
    }

    public void download() {
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() == 0) {
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threadInfos.get(0);
        }
        new DownLoadThread(threadInfo).start();
    }

    class DownLoadThread extends Thread {
        private ThreadInfo mThreadInfo = null;

        public DownLoadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "byte = " + start + "-" + mThreadInfo.getEnd());
                File file = new File(DownLoadService.DOWNLOADPATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent(DownLoadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                Log.d("xyn", conn.getResponseCode() + "");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                } else if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    Log.d("xyn", "HTTP_BAD_REQUEST");
                    input = conn.getInputStream();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.disconnect();
                    input.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
