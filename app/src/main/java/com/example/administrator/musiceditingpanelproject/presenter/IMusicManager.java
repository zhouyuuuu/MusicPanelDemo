package com.example.administrator.musiceditingpanelproject.presenter;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/27.
 */

public interface IMusicManager {
    void refreshMusicEditPanel();

    void musicGroupListDataLoadedCallback(ArrayList<MusicGroup> musicGroups);

    void musicGroupListDataLoadedFailedCallback();

    void downloadMusic(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder,String sort,int page,int position);

    void musicFileDataLoadedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder, String sort, int page, int position);

    void musicFileDataLoadedFailedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder);

    void deleteMusic(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder);

    void musicFileDataDeletedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder);

    void musicFileDataDeletedFailedCallback(MusicBean musicBean);

    void playMusic(MusicBean musicBean);

    void stopMusic();

    void replayMusic();

}
