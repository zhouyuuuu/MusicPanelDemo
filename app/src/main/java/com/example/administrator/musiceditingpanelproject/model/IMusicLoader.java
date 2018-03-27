package com.example.administrator.musiceditingpanelproject.model;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter.ItemHolder;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;

/**
 * 音乐加载器接口
 * Edited by Administrator on 2018/3/27.
 */

public interface IMusicLoader {
    /**
     * 加载音频分组列表数据
     */
    void loadMusicGroupListData();

    /**
     * 加载音频文件
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     * @param sort       分类
     * @param page       第几页
     * @param position   第几个
     */
    void loadMusicFileData(MusicBean musicBean, ItemHolder itemHolder, String sort, int page, int position);

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     */
    void deleteMusicFile(MusicBean musicBean, ItemHolder itemHolder);
}
