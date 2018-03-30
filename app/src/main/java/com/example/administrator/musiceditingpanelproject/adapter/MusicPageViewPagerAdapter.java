package com.example.administrator.musiceditingpanelproject.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.musiceditingpanelproject.R;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.util.ArrayList;

/**
 * ViewPager的适配器
 * Edited by Administrator on 2018/3/25.
 */

public class MusicPageViewPagerAdapter extends PagerAdapter implements MusicListRecyclerViewAdapter.ItemClickListener {

    // 默认每页的音频信息数目为6
    public static final int ITEM_COUNT_PER_PAGE = 6;
    // Item列数
    private static final int SPAN_COUNT = 3;
    // 音频信息分组
    private MusicGroup mMusicGroup;
    // 总页数
    private int mPageCount;
    // 每页对应的View
    private ArrayList<View> mViews;
    // 每页对应的RecyclerView
    private ArrayList<RecyclerView> mRecyclerViews;
    // 音频ITEM监听器
    private MusicItemClickListener mMusicItemClickListener;


    public MusicPageViewPagerAdapter(ViewPager viewPager, MusicGroup musicGroup) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(MusicEditingPanelApplication.getApplication());
        mViews = new ArrayList<>();
        mRecyclerViews = new ArrayList<>();
        this.mMusicGroup = musicGroup;
        // 拿到所有的musicBean
        ArrayList<MusicBean> allMusicBeans = musicGroup.getMusicBeans();
        // 计算总页数
        mPageCount = calculatePageCount(allMusicBeans.size());
        // 每6个MusicBean分为一页，每一页创建一个view
        for (int i = 0; i < mPageCount; i++) {
            View view = mLayoutInflater.inflate(R.layout.edit_music_viewpager, viewPager, false);
            RecyclerView recyclerView = view.findViewById(R.id.rv_items);
            ArrayList<MusicBean> musicBeans = new ArrayList<>();
            // 分配1-6个Item
            for (int j = i * ITEM_COUNT_PER_PAGE; j < (i + 1) * ITEM_COUNT_PER_PAGE; j++) {
                if (j >= allMusicBeans.size()) break;
                musicBeans.add(allMusicBeans.get(j));
            }
            MusicListRecyclerViewAdapter musicListRecyclerViewAdapter = new MusicListRecyclerViewAdapter(musicBeans, i);
            musicListRecyclerViewAdapter.setItemClickListener(this);
            recyclerView.setAdapter(musicListRecyclerViewAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(MusicEditingPanelApplication.getApplication(), SPAN_COUNT));
            mRecyclerViews.add(recyclerView);
            mViews.add(view);
        }
    }

    /**
     * 计算总共多少页
     * @param allItemCount Item总数
     * @return 页数
     */
    private int calculatePageCount(int allItemCount){
        int pageCount = allItemCount / ITEM_COUNT_PER_PAGE;
        if (allItemCount % ITEM_COUNT_PER_PAGE > 0) pageCount++;
        return pageCount;
    }

    public int getPageCount() {
        return mPageCount;
    }

    public void setMusicItemClickListener(MusicItemClickListener musicItemClickListener) {
        this.mMusicItemClickListener = musicItemClickListener;
    }

    public ArrayList<RecyclerView> getRecyclerViews() {
        return mRecyclerViews;
    }

    /**
     * 这边从每个RecyclerView那边回调，将回调内容交给实现了MusicItemClickListener的Activity处理
     */
    @Override
    public void OnItemClick(int position, MusicListRecyclerViewAdapter.ItemHolder holder, MusicBean musicBean, int pageIndex) {
        if (mMusicItemClickListener != null) {
            mMusicItemClickListener.onMusicItemClicked(position, holder, musicBean, pageIndex, mMusicGroup.getSortName());
        }
    }

    @Override
    public int getCount() {
        return mViews == null ? 0 : mViews.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViews.get(position));
    }

    public interface MusicItemClickListener {
        void onMusicItemClicked(int position, MusicListRecyclerViewAdapter.ItemHolder holder, MusicBean musicBean, int pageIndex, String sort);
    }


}
