package com.example.administrator.musiceditingpanelproject.module.editmusic.presenter;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.module.editmusic.model.EditMusicPanelLoader;
import com.example.administrator.musiceditingpanelproject.module.editmusic.model.EditMusicPanelPlayer;
import com.example.administrator.musiceditingpanelproject.module.editmusic.model.IMusicLoader;
import com.example.administrator.musiceditingpanelproject.module.editmusic.model.IMusicPlayer;
import com.example.administrator.musiceditingpanelproject.module.editmusic.view.IEditMusicPanel;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelManager implements IMusicManager {

    private IMusicLoader mMusicLoader;
    private IMusicPlayer mMusicPlayer;
    private IEditMusicPanel mEditMusicPanel;

    public EditMusicPanelManager(IEditMusicPanel iEditMusicPanel) {
        mEditMusicPanel = iEditMusicPanel;
        mMusicPlayer = new EditMusicPanelPlayer();
        mMusicLoader = EditMusicPanelLoader.getInstance();
        mMusicLoader.registerMusicManager(this);
    }

    /**
     * 刷新音频展示面板，先获取音乐列表数据，获取后会回调面板刷新
     */
    @Override
    public void loadMusicGroupListData() {
        mMusicLoader.loadMusicGroupListData();
    }

    /**
     * 音频信息分组列表数据加载成功回调
     *
     * @param musicGroups 音频信息分组列表
     */
    @Override
    public void musicGroupListDataLoadedCallback(ArrayList<MusicGroup> musicGroups) {
        mEditMusicPanel.musicGroupListLoadedCallback(musicGroups);
    }

    /**
     * 音频信息分组列表数据加载失败回调
     */
    @Override
    public void musicGroupListDataLoadedFailedCallback() {
        mEditMusicPanel.musicGroupListDataLoadedFailedCallback();
    }

    /**
     * 下载音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void downloadMusicFile(MusicBean musicBean) {
        // 先让View设置为下载中
        musicBean.setState(MusicBean.STATE_DOWNLOADING);
        mEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        // 让loader去下载
        mMusicLoader.loadMusicFileData(musicBean);
    }

    /**
     * 下载音频文件成功回调，判断是否url与View有绑定关系，没有则说明View被别的url重用了，则不通知面板进行更新
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileLoadedCallback(MusicBean musicBean) {
        // 设置为下载完成
        musicBean.setState(MusicBean.STATE_DOWNLOADED);
        mEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        // 下载成功回调，让View去处理下载完成的Bean哪个要播放音乐
        mEditMusicPanel.musicFileDataLoadedCallback(musicBean);
    }

    /**
     * 下载音频文件失败回调
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileLoadedFailedCallback(MusicBean musicBean) {
        // 更新View为未下载
        musicBean.setState(MusicBean.STATE_UNDOWNLOADED);
        mEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        // 通知有任务失败
        mEditMusicPanel.musicFileDataLoadedFailedCallback(musicBean);
    }

    /**
     * 暂停下载
     * @param musicBean  音频信息
     */
    @Override
    public void pauseDownloadMusicFile(MusicBean musicBean) {
        mMusicLoader.pauseLoading(musicBean);
    }

    /**
     * 暂停的回调
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileLoadingPausedCallback(MusicBean musicBean) {
        // View设置为暂停
        musicBean.setState(MusicBean.STATE_DOWNLOAD_PAUSED);
        mEditMusicPanel.musicBeanStateChangedCallback(musicBean);
    }

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void deleteMusicFile(MusicBean musicBean) {
        mMusicLoader.deleteMusicFile(musicBean);
    }

    /**
     * 删除音频文件成功回调
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileDeletedCallback(MusicBean musicBean) {
        // 设置为未下载状态
        musicBean.setState(MusicBean.STATE_UNDOWNLOADED);
        mEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        // 删除成功
        mEditMusicPanel.musicFileDataDeletedCallback(musicBean);
    }

    /**
     * 删除音频文件失败回调
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicFileDeletedFailedCallback(MusicBean musicBean) {
        // 删除失败所以文件还在，设置为已下载状态
        musicBean.setState(MusicBean.STATE_DOWNLOADED);
        mEditMusicPanel.musicBeanStateChangedCallback(musicBean);
        // 删除失败
        mEditMusicPanel.musicFileDataDeletedFailedCallback(musicBean);
    }

    /**
     * 暂停加载音乐
     */
    @Override
    public void panelOnDestroy() {
        mMusicLoader.unregisterMusicManager(this);
    }

    /**
     * 播放音乐
     *
     * @param musicBean 音频信息
     */
    @Override
    public void playMusic(MusicBean musicBean) {
        mMusicPlayer.playMusic(musicBean);
    }

    /**
     * 停止音乐
     */
    @Override
    public void pauseMusic() {
        mMusicPlayer.pauseMusic();
    }

    /**
     * 重播音乐
     */
    @Override
    public void restartMusic() {
        mMusicPlayer.restartMusic();
    }

    @Override
    public void resetPlayer() {
        mMusicPlayer.resetPlayer();
    }

    @Override
    public void stopPlayer() {
        mMusicPlayer.stopPlayer();
    }

}
