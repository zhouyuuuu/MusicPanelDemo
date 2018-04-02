package com.example.administrator.musiceditingpanelproject.module.editmusic.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.musiceditingpanelproject.R;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.common.view.DownloadIconView;
import com.example.administrator.musiceditingpanelproject.common.view.DownloadPausedIconView;
import com.example.administrator.musiceditingpanelproject.common.view.IndeterminateProgressBar;

import java.util.ArrayList;

/**
 * ViewPager中音频列表RecyclerView的适配器
 * Edited by Administrator on 2018/3/25.
 */

public class MusicListRecyclerViewAdapter extends RecyclerView.Adapter<MusicListRecyclerViewAdapter.ItemHolder> {
    // 展示的音频信息组
    private final ArrayList<MusicBean> mMusicBeans;
    // 布局加载器，作为全局变量就不用每次都去调用from方法，降低ViewHolder创建效率
    private LayoutInflater mLayoutInflater;
    // Item点击监听器
    private ItemClickListener mItemClickListener;

    MusicListRecyclerViewAdapter(ArrayList<MusicBean> musicBeans) {
        // 成员变量初始化
        this.mLayoutInflater = LayoutInflater.from(MusicEditingPanelApplication.getApplication());
        this.mMusicBeans = musicBeans;
    }


    @NonNull
    @Override
    public MusicListRecyclerViewAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.edit_music_item_recyclerview, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicListRecyclerViewAdapter.ItemHolder holder, final int position) {
        MusicBean musicBean = mMusicBeans.get(position);
        switch (musicBean.getState()) {
            case MusicBean.STATE_UNDOWNLOADED:
                holder.showUndownloadedState();
                break;
            case MusicBean.STATE_DOWNLOADING:
                holder.showDownloadingState();
                break;
            case MusicBean.STATE_DOWNLOADED:
                holder.showDownloadedState();
                break;
            case MusicBean.STATE_PLAYING:
                holder.showPlayingState();
                break;
            case MusicBean.STATE_DOWNLOAD_PAUSED:
                holder.showDownloadPausedState();
                break;
            case MusicBean.STATE_PLAYING_PAUSED:
                holder.showPlayingPausedState();
                break;
            default:
                break;
        }
        holder.mTvSongName.setText(musicBean.getName());
        holder.mTvAuthorName.setText(musicBean.getAuthorName());
        // Item点击事件的监听器在这里触发
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.OnItemClick(holder, mMusicBeans.get(holder.getAdapterPosition()));
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
        return mMusicBeans == null ? 0 : mMusicBeans.size();
    }

    void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    /**
     * Item点击监听器
     */
    public interface ItemClickListener {
        void OnItemClick(ItemHolder holder, MusicBean musicBean);
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        private TextView mTvSongName;
        private TextView mTvAuthorName;
        private IndeterminateProgressBar mIndeterminateProgressBar;
        private DownloadIconView mDownloadIconView;
        private TextView mTvEdit;
        private DownloadPausedIconView mDownloadPausedIconView;

        ItemHolder(View itemView) {
            super(itemView);
            mTvSongName = itemView.findViewById(R.id.tv_song_name);
            mTvAuthorName = itemView.findViewById(R.id.tv_author_name);
            mIndeterminateProgressBar = itemView.findViewById(R.id.indeterminate_progress_bar);
            mDownloadIconView = itemView.findViewById(R.id.download_view);
            mTvEdit = itemView.findViewById(R.id.tv_edit);
            mDownloadPausedIconView = itemView.findViewById(R.id.download_paused_view);
        }

        /**
         * 展示编辑状态
         */
        public void showPlayingState() {
            itemView.setBackgroundResource(R.mipmap.edit_music_item_selected_bg);
            mTvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_downloaded));
            mIndeterminateProgressBar.setVisibility(View.GONE);
            mDownloadIconView.setVisibility(View.GONE);
            mTvAuthorName.setVisibility(View.GONE);
            mTvSongName.setVisibility(View.GONE);
            mTvEdit.setVisibility(View.VISIBLE);
            mTvEdit.setText(MusicEditingPanelApplication.getApplication().getString(R.string.edit_play));
            mDownloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示未下载状态
         */
        public void showUndownloadedState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            mTvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_undownloaded));
            mIndeterminateProgressBar.setVisibility(View.GONE);
            mDownloadIconView.setVisibility(View.VISIBLE);
            mTvAuthorName.setVisibility(View.VISIBLE);
            mTvSongName.setVisibility(View.VISIBLE);
            mTvEdit.setVisibility(View.GONE);
            mDownloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示下载中状态
         */
        public void showDownloadingState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            mTvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_undownloaded));
            mIndeterminateProgressBar.setVisibility(View.VISIBLE);
            mDownloadIconView.setVisibility(View.GONE);
            mTvAuthorName.setVisibility(View.VISIBLE);
            mTvSongName.setVisibility(View.VISIBLE);
            mTvEdit.setVisibility(View.GONE);
            mDownloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示已下载状态
         */
        public void showDownloadedState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            mTvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_downloaded));
            mIndeterminateProgressBar.setVisibility(View.GONE);
            mDownloadIconView.setVisibility(View.GONE);
            mTvAuthorName.setVisibility(View.VISIBLE);
            mTvSongName.setVisibility(View.VISIBLE);
            mTvEdit.setVisibility(View.GONE);
            mDownloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示下载暂停状态
         */
        public void showDownloadPausedState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            mTvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_undownloaded));
            mIndeterminateProgressBar.setVisibility(View.GONE);
            mDownloadIconView.setVisibility(View.GONE);
            mTvAuthorName.setVisibility(View.VISIBLE);
            mTvSongName.setVisibility(View.VISIBLE);
            mTvEdit.setVisibility(View.GONE);
            mDownloadPausedIconView.setVisibility(View.VISIBLE);
        }

        /**
         * 展示播放暂停状态
         */
        public void showPlayingPausedState() {
            itemView.setBackgroundResource(R.mipmap.edit_music_item_selected_bg);
            mTvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_downloaded));
            mIndeterminateProgressBar.setVisibility(View.GONE);
            mDownloadIconView.setVisibility(View.GONE);
            mTvAuthorName.setVisibility(View.GONE);
            mTvSongName.setVisibility(View.GONE);
            mTvEdit.setVisibility(View.VISIBLE);
            mTvEdit.setText(MusicEditingPanelApplication.getApplication().getString(R.string.edit_pause));
            mDownloadPausedIconView.setVisibility(View.GONE);
        }

    }


}
