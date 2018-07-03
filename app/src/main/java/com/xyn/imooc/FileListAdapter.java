package com.xyn.imooc;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xyn.imooc.entry.FileInfo;
import com.xyn.imooc.service.DownLoadService;

import java.util.List;

public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> mFileList;

    public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
        super();
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.tvFile = view.findViewById(R.id.tvfileName);
            holder.btStop = view.findViewById(R.id.btStop);
            holder.btStart = view.findViewById(R.id.btStart);
            holder.pbFile = view.findViewById(R.id.pbProgress);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final FileInfo fileInfo = mFileList.get(position);
        holder.tvFile.setText(fileInfo.getFileName());
        holder.pbFile.setMax(100);
        holder.pbFile.setProgress(fileInfo.getFinished());
        holder.btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });

        holder.btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });
        return view;
    }

    public void updateProgress(int id, int progress) {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView tvFile;
        Button btStop;
        Button btStart;
        ProgressBar pbFile;
    }
}
