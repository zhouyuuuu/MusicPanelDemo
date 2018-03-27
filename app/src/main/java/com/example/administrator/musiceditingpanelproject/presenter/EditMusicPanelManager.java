package com.example.administrator.musiceditingpanelproject.presenter;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.model.EditMusicPanelLoader;
import com.example.administrator.musiceditingpanelproject.model.EditMusicPanelPlayer;
import com.example.administrator.musiceditingpanelproject.model.IMusicLoader;
import com.example.administrator.musiceditingpanelproject.model.IMusicPlayer;
import com.example.administrator.musiceditingpanelproject.util.BindUtil;
import com.example.administrator.musiceditingpanelproject.view.IEditMusicPanel;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelManager implements IMusicManager {

    private IMusicLoader iMusicLoader;
    private IMusicPlayer iMusicPlayer;
    private IEditMusicPanel iEditMusicPanel;

    public EditMusicPanelManager(IEditMusicPanel iEditMusicPanel) {
        this.iEditMusicPanel = iEditMusicPanel;
        iMusicPlayer = new EditMusicPanelPlayer();
        iMusicLoader = new EditMusicPanelLoader(this);
    }

    @Override
    public void refreshMusicEditPanel() {
        iMusicLoader.loadMusicGroupListData();
    }

    @Override
    public void musicGroupListDataLoadedCallback(ArrayList<MusicGroup> musicGroups) {
        iEditMusicPanel.musicGroupListLoadedCallback(musicGroups);
    }

    @Override
    public void musicGroupListDataLoadedFailedCallback() {
        iEditMusicPanel.musicGroupListDataLoadedFailedCallback();
    }

    @Override
    public void downloadMusic(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder,String sort,int page,int position) {
        BindUtil.bindUrlAndView(itemHolder.itemView,musicBean.getUrl());
        musicBean.setState(MusicBean.STATE_DOWNLOADING);
        iEditMusicPanel.musicBeanStateChangedCallback(itemHolder,musicBean);
        iMusicLoader.loadMusicFileData(musicBean,itemHolder,sort,page,position);
    }

    @Override
    public void musicFileDataLoadedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder,String sort,int page,int position) {
        if (BindUtil.isBound(itemHolder.itemView,musicBean.getUrl())){
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder,musicBean);
            iEditMusicPanel.musicFileDataLoadedCallback(musicBean,itemHolder,sort,page,position);
        }
    }

    @Override
    public void musicFileDataLoadedFailedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder) {
        if (BindUtil.isBound(itemHolder.itemView,musicBean.getUrl())){
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder,musicBean);
        }
        iEditMusicPanel.musicFileDataLoadedFailedCallback(musicBean);
    }

    @Override
    public void deleteMusic(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder) {
        BindUtil.bindUrlAndView(itemHolder.itemView,musicBean.getUrl());
        iMusicLoader.deleteMusicFile(musicBean,itemHolder);
    }

    @Override
    public void musicFileDataDeletedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder) {
        if (BindUtil.isBound(itemHolder.itemView,musicBean.getUrl())) {
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder, musicBean);
        }
        iEditMusicPanel.musicFileDataDeletedCallback(musicBean);
    }

    @Override
    public void musicFileDataDeletedFailedCallback(MusicBean musicBean) {
        iEditMusicPanel.musicFileDataDeletedFailedCallback(musicBean);
    }

    @Override
    public void playMusic(MusicBean musicBean) {
        iMusicPlayer.playMusic(musicBean);
    }

    @Override
    public void stopMusic() {
        iMusicPlayer.stopMusic();
    }

    @Override
    public void replayMusic() {
        iMusicPlayer.replayMusic();
    }

}
