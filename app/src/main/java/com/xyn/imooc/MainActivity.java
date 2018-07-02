package com.xyn.imooc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xyn.imooc.entry.FileInfo;
import com.xyn.imooc.service.DownLoadService;

public class MainActivity extends AppCompatActivity {

    private TextView mTvFileName = null;
    private ProgressBar mPbProgress = null;
    private Button mBtStop = null;
    private Button mBtStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvFileName = findViewById(R.id.tvfileName);
        mPbProgress = findViewById(R.id.pbProgress);
        mBtStop = findViewById(R.id.btStop);
        mBtStart = findViewById(R.id.btStart);

        mPbProgress.setMax(100);
        final FileInfo fileInfo = new FileInfo
                (0, "http://dlsw.baidu.com/sw-search-sp/soft/1a/11798/kugou_V7.6.85.17344_setup.1427079848.exe",
                        "kugou_V7.6.85.17344_setup.1427079848.exe", 0, 0);

        mTvFileName.setText(fileInfo.getFileName());
        mBtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });

        mBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownLoadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                mPbProgress.setProgress(finished);
            }
        }
    };
}
