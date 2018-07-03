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
import java.util.ArrayList;
import java.util.List;


public class DownLoadTask {
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDao = null;
    private int mFinished = 0;
    public boolean isPause = false;
    private int mThreadCount = 1; //线程数量
    private List<DownLoadThread> mThreadList = null;//线程集合

    public DownLoadTask(Context mContext, FileInfo mFileInfo, int mThreadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mThreadCount = mThreadCount;
        mDao = new ThreadDAOImpl(mContext);
    }

    public void download() {
        List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
        if (threads.size() == 0) {
            int length = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),
                        length * i, (i + 1) * length - 1, 0);
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threads.add(threadInfo);
                mDao.insertThread(threadInfo);
            }
        }
        mThreadList = new ArrayList<>();
        for (ThreadInfo info : threads) {
            DownLoadThread thread = new DownLoadThread(info);
            thread.start();
            mThreadList.add(thread);
        }
    }

    /*判断所有线程都执行完毕*/
    private synchronized void checkAllThreadsFinshed() {
        boolean allFinished = true;
        for (DownLoadThread thread : mThreadList) {
            if (thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            mDao.deleteThread(mFileInfo.getUrl());
            Intent intent = new Intent(DownLoadService.ACTION_FINISH);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
            Log.d("xyn", "ACTION_FINISH to sendBroadcast");
        }
    }

    class DownLoadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        public boolean isFinished = false; //标识线程是否结束

        public DownLoadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
//            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
//                mDao.insertThread(mThreadInfo);
//            }
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
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(),
                                    mThreadInfo.getId(),
                                    mThreadInfo.getFinished());
                            return;
                        }
                    }
                    isFinished = true;
                    checkAllThreadsFinshed();
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
