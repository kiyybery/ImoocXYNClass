package com.xyn.imooc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xyn.imooc.entry.FileInfo;
import com.xyn.imooc.service.DownLoadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mLvFile = null;
    private List<FileInfo> mFileList = null;
    private FileListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLvFile = findViewById(R.id.lvFile);

        mFileList = new ArrayList<>();
        //http://www.imooc.com/mobile/imooc.apk
        //http://www.imooc.com/download/Activator.exe
        //http://www.imooc.com/download/Itunes64Setup.exe
        //http://www.imooc.com/download/BaiduPlayerNetSetup_100.exe
        //http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe

        FileInfo fileInfo = new FileInfo(0, "http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe"
                , "kugou_V7.6.85.17344_setup.1427079848.exe", 0, 0);
        FileInfo fileInfo1 = new FileInfo(0, "http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe"
                , "kugou_V7.6.85.17344_setup.exe", 0, 0);
        FileInfo fileInfo2 = new FileInfo(0, "http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe"
                , "kugou_V7.6.85.17344.exe", 0, 0);
        FileInfo fileInfo3 = new FileInfo(0, "http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe"
                , "kugou.exe", 0, 0);

        mFileList.add(fileInfo);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mAdapter = new FileListAdapter(this, mFileList);
        mLvFile.setAdapter(mAdapter);

        /*IntentFilter filter = new IntentFilter();
        filter.addAction(DownLoadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
    }

//    BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) {
//                int finished = intent.getIntExtra("finished", 0);
//                mPbProgress.setProgress(finished);
//            }
//        }
//    };
}
