package com.example.administrator.musiceditingpanelproject.presenter;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.model.EditMusicPanelLoader;
import com.example.administrator.musiceditingpanelproject.model.EditMusicPanelPlayer;
import com.example.administrator.musiceditingpanelproject.model.IMusicLoader;
import com.example.administrator.musiceditingpanelproject.model.IMusicPlayer;
import com.example.administrator.musiceditingpanelproject.view.IEditMusicPanel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelManager implements IMusicManager {

    private IMusicLoader iMusicLoader;
    private IMusicPlayer iMusicPlayer;
    private WeakReference<IEditMusicPanel> iEditMusicPanelWeakReference;

    public EditMusicPanelManager(IEditMusicPanel iEditMusicPanel) {
        this.iEditMusicPanelWeakReference = new WeakReference<>(iEditMusicPanel);
        iMusicPlayer = new EditMusicPanelPlayer();
        iMusicLoader = new EditMusicPanelLoader(this);
    }

    /**
     * 刷新音频展示面板，先获取音乐列表数据，获取后会回调面板刷新
     */
    @Override
    public void refreshMusicEditPanel() {
        iMusicLoader.loadMusicGroupListData();
    }

    /**
     * 音频信息分组列表数据加载成功回调
     *
     * @param musicGroups 音频信息分组列表
     */
    @Override
    public void musicGroupListDataLoadedCallback(ArrayList<MusicGroup> musicGroups) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicGroupListLoadedCallback(musicGroups);
    }

    /**
     * 音频信息分组列表数据加载失败回调
     */
    @Override
    public void musicGroupListDataLoadedFailedCallback() {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicGroupListDataLoadedFailedCallback();
    }

    /**
     * 下载音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void downloadMusic(MusicBean musicBean) {
        musicBean.setState(MusicBean.STATE_DOWNLOADING);
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        iMusicLoader.loadMusicFileData(musicBean);
    }

    /**
     * 下载音频文件成功回调，判断是否url与View有绑定关系，没有则说明View被别的url重用了，则不通知面板进行更新
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileDataLoadedCallback(MusicBean musicBean) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        iEditMusicPanel.musicFileDataLoadedCallback(musicBean);
    }

    /**
     * 下载音频文件失败回调
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileDataLoadedFailedCallback(MusicBean musicBean) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        iEditMusicPanel.musicFileDataLoadedFailedCallback(musicBean);
    }

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void deleteMusic(MusicBean musicBean) {
        iMusicLoader.deleteMusicFile(musicBean);
    }

    /**
     * 删除音频文件成功回调
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileDataDeletedCallback(MusicBean musicBean) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        iEditMusicPanel.musicFileDataDeletedCallback(musicBean);
    }

    /**
     * 删除音频文件失败回调
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicFileDataDeletedFailedCallback(MusicBean musicBean) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        iEditMusicPanel.musicFileDataDeletedFailedCallback(musicBean);
    }

    /**
     * 播放音乐
     *
     * @param musicBean 音频信息
     */
    @Override
    public void playMusic(MusicBean musicBean) {
        iMusicPlayer.playMusic(musicBean);
    }

    /**
     * 停止音乐
     */
    @Override
    public void stopMusic() {
        iMusicPlayer.stopMusic();
    }

    /**
     * 重播音乐
     */
    @Override
    public void replayMusic() {
        iMusicPlayer.replayMusic();
    }

}
