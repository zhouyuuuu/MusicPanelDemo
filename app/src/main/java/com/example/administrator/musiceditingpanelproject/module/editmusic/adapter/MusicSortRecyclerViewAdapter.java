package com.example.administrator.musiceditingpanelproject.module.editmusic.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.musiceditingpanelproject.R;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.util.ArrayList;

/**
 * 底部分类列表RecyclerView的适配器
 * Edited by Administrator on 2018/3/25.
 */

public class MusicSortRecyclerViewAdapter extends RecyclerView.Adapter<MusicSortRecyclerViewAdapter.SortHolder> {
    // 展示的图片数据
    private final ArrayList<MusicGroup> mMusicGroups;
    // 布局加载器，作为全局变量就不用每次都去调用from方法，降低ViewHolder创建效率
    private LayoutInflater mLayoutInflater;
    // Item点击监听器
    private ItemClickListener mItemClickListener;

    public MusicSortRecyclerViewAdapter(ArrayList<MusicGroup> data) {
        // 成员变量初始化
        this.mLayoutInflater = LayoutInflater.from(MusicEditingPanelApplication.getApplication());
        this.mMusicGroups = data;
    }


    @NonNull
    @Override
    public SortHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.edit_music_sort_recyclerview, parent, false);
        return new SortHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SortHolder holder, int position) {
        holder.mTextView.setText(mMusicGroups.get(position).getSortName());
        // Item点击事件的监听器在这里触发
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(holder.getAdapterPosition(), holder);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mMusicGroups == null ? 0 : mMusicGroups.size();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    /**
     * Item点击监听器
     */
    public interface ItemClickListener {
        void onItemClick(int position, SortHolder holder);
    }

    public static class SortHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        SortHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv_sort);
        }

        public void showUnClickedState() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mTextView.setBackground(null);
            } else {
                mTextView.setBackgroundDrawable(null);
            }
        }

        public void showClickedState() {
            mTextView.setBackgroundResource(R.color.edit_music_panel_bg);
        }
    }
}
