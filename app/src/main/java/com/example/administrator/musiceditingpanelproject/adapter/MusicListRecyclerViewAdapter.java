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
import com.example.administrator.musiceditingpanelproject.customview.DownloadIconView;
import com.example.administrator.musiceditingpanelproject.customview.IndeterminateProgressBar;
import com.example.administrator.musiceditingpanelproject.util.CacheUtil;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * ViewPager中音频列表RecyclerView的适配器
 * Edited by Administrator on 2018/3/25.
 */

public class MusicListRecyclerViewAdapter extends RecyclerView.Adapter<MusicListRecyclerViewAdapter.ItemHolder> {
    // 歌名白色
    private static final int COLOR_SONG_NAME_DOWNLOADED = 0xffffffff;
    // 歌名灰色
    private static final int COLOR_SONG_NAME_UNDOWNLOADED = 0xff848484;
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
        View v = mLayoutInflater.inflate(R.layout.recyclerview_music_item, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicListRecyclerViewAdapter.ItemHolder holder, final int position) {
        MusicBean musicBean = mMusicBeans.get(position);
        // 每次都先将控件初始化，避免View复用携带了之前的数据
        switch (musicBean.getState()){
            case MusicBean.STATE_UNDOWNLOADED:
                holder.showUndownloadedState();
                break;
            case MusicBean.STATE_DOWNLOADING:
                holder.showDownloadingState();
                break;
            case MusicBean.STATE_DOWNLOADED:
                holder.showDownloadedState();
                break;
            case MusicBean.STATE_EDIT:
                holder.showEditState();
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
        private View vFrame;
        private TextView tvEdit;

        ItemHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tv_song_name);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            indeterminateProgressBar = itemView.findViewById(R.id.indeterminate_progress_bar);
            downloadIconView = itemView.findViewById(R.id.download_view);
            vFrame = itemView.findViewById(R.id.v_frame);
            tvEdit = itemView.findViewById(R.id.tv_edit);
        }

        /**
         * 展示编辑状态
         */
        public void showEditState() {
            tvSongName.setTextColor(COLOR_SONG_NAME_DOWNLOADED);
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.GONE);
            tvSongName.setVisibility(View.GONE);
            vFrame.setVisibility(View.VISIBLE);
            tvEdit.setVisibility(View.VISIBLE);
        }

        /**
         * 展示未下载状态
         */
        public void showUndownloadedState() {
            tvSongName.setTextColor(COLOR_SONG_NAME_UNDOWNLOADED);
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.VISIBLE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            vFrame.setVisibility(View.GONE);
            tvEdit.setVisibility(View.GONE);
        }

        /**
         * 展示下载中状态
         */
        public void showDownloadingState() {
            tvSongName.setTextColor(COLOR_SONG_NAME_UNDOWNLOADED);
            indeterminateProgressBar.setVisibility(View.VISIBLE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            vFrame.setVisibility(View.GONE);
            tvEdit.setVisibility(View.GONE);
        }

        /**
         * 展示已下载状态
         */
        public void showDownloadedState() {
            tvSongName.setTextColor(COLOR_SONG_NAME_DOWNLOADED);
            indeterminateProgressBar.setVisibility(View.GONE);
            downloadIconView.setVisibility(View.GONE);
            tvAuthorName.setVisibility(View.VISIBLE);
            tvSongName.setVisibility(View.VISIBLE);
            vFrame.setVisibility(View.GONE);
            tvEdit.setVisibility(View.GONE);
        }

    }


}
