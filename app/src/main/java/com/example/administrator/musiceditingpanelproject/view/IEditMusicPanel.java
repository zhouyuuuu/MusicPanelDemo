package com.example.administrator.musiceditingpanelproject.view;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/27.
 */

public interface IEditMusicPanel {
    void musicBeanStateChangedCallback(MusicListRecyclerViewAdapter.ItemHolder holder, MusicBean musicBean);

    /**
     * 这个方法会被异步回调
     *
     * @param musicGroups 音频信息分组列表
     */
    void musicGroupListLoadedCallback(ArrayList<MusicGroup> musicGroups);

    /**
     * 这个方法会被异步回调
     */
    void musicGroupListDataLoadedFailedCallback();

    void musicFileDataLoadedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder,String sort,int page,int position);

    void musicFileDataLoadedFailedCallback(MusicBean musicBean);

    void musicFileDataDeletedCallback(MusicBean musicBean);

    void musicFileDataDeletedFailedCallback(MusicBean musicBean);
}
