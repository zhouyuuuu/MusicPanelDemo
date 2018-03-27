package com.example.administrator.musiceditingpanelproject.model;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;

/**
 * Edited by Administrator on 2018/3/27.
 */

public interface IMusicLoader {
    void loadMusicGroupListData();
    void loadMusicFileData(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder,String sort,int page,int position);
    void deleteMusicFile(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder);
}
