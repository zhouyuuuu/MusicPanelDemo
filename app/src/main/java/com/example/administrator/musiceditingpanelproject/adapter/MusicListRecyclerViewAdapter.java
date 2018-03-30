package com.example.administrator.musiceditingpanelproject.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.musiceditingpanelproject.R;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.widget.DownloadIconView;
import com.example.administrator.musiceditingpanelproject.widget.DownloadPausedIconView;
import com.example.administrator.musiceditingpanelproject.widget.IndeterminateProgressBar;

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
    // 第几页的RecyclerView的页码
    private int mPageIndex;

    MusicListRecyclerViewAdapter(ArrayList<MusicBean> musicBeans, int pageIndex) {
        // 成员变量初始化
        this.mLayoutInflater = LayoutInflater.from(MusicEditingPanelApplication.getApplication());
        this.mMusicBeans = musicBeans;
        mPageIndex = pageIndex;
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
        holder.tvSongName.setText(musicBean.getName());
        holder.tvAuthorName.setText(musicBean.getAuthorName());
        // Item点击事件的监听器在这里触发
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.OnItemClick(holder.getAdapterPosition(), holder, mMusicBeans.get(holder.getAdapterPosition()), mPageIndex);
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
        void OnItemClick(int position, ItemHolder holder, MusicBean musicBean, int pageIndex);
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        private TextView tvSongName;
        private TextView tvAuthorName;
        private IndeterminateProgressBar indeterminateProgressBar;
        private DownloadIconView downloadIconView;
        private TextView tvEdit;
        private DownloadPausedIconView downloadPausedIconView;

        ItemHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tv_song_name);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            indeterminateProgressBar = itemView.findViewById(R.id.indeterminate_progress_bar);
            downloadIconView = itemView.findViewById(R.id.download_view);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            downloadPausedIconView = itemView.findViewById(R.id.download_paused_view);
        }

        /**
         * 展示编辑状态
         */
        public void showPlayingState() {
            itemView.setBackgroundResource(R.mipmap.edit_music_item_selected_bg);
            tvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_downloaded));
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.GONE);
            tvSongName.setVisibility(View.GONE);
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setText(MusicEditingPanelApplication.getApplication().getString(R.string.edit_play));
            downloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示未下载状态
         */
        public void showUndownloadedState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            tvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_undownloaded));
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.VISIBLE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            tvEdit.setVisibility(View.GONE);
            downloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示下载中状态
         */
        public void showDownloadingState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            tvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_undownloaded));
            indeterminateProgressBar.setVisibility(View.VISIBLE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            tvEdit.setVisibility(View.GONE);
            downloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示已下载状态
         */
        public void showDownloadedState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            tvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_downloaded));
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            tvEdit.setVisibility(View.GONE);
            downloadPausedIconView.setVisibility(View.GONE);
        }

        /**
         * 展示下载暂停状态
         */
        public void showDownloadPausedState() {
            itemView.setBackgroundResource(R.color.edit_music_item_bg);
            tvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_undownloaded));
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            tvEdit.setVisibility(View.GONE);
            downloadPausedIconView.setVisibility(View.VISIBLE);
        }

        /**
         * 展示播放暂停状态
         */
        public void showPlayingPausedState() {
            itemView.setBackgroundResource(R.mipmap.edit_music_item_selected_bg);
            tvSongName.setTextColor(MusicEditingPanelApplication.getApplication().getResources().getColor(R.color.edit_music_name_song_text_downloaded));
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.GONE);
            tvSongName.setVisibility(View.GONE);
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setText(MusicEditingPanelApplication.getApplication().getString(R.string.edit_pause));
            downloadPausedIconView.setVisibility(View.GONE);
        }

    }


}
