package com.example.administrator.musiceditingpanelproject.module.editmusic.presenter;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.util.ArrayList;

/**
 * 音乐业务管理器
 * Edited by Administrator on 2018/3/27.
 */

public interface IMusicManager {
    /**
     * 刷新音频展示面板，应该在这里做一些获取音乐列表数据的操作并回调面板刷新
     */
    void loadMusicGroupListData();

    /**
     * 音频信息分组列表数据加载成功回调
     *
     * @param musicGroups 音频信息分组列表
     */
    void musicGroupListDataLoadedCallback(ArrayList<MusicGroup> musicGroups);

    /**
     * 音频信息分组列表数据加载失败回调
     */
    void musicGroupListDataLoadedFailedCallback();

    /**
     * 下载音频文件
     *
     * @param musicBean  音频信息
     */
    void downloadMusicFile(MusicBean musicBean);

    /**
     * 下载音频文件成功回调
     *
     * @param musicBean  音频信息
     */
    void musicFileLoadedCallback(MusicBean musicBean);

    /**
     * 下载音频文件失败回调
     *
     * @param musicBean  音频信息
     */
    void musicFileLoadedFailedCallback(MusicBean musicBean);

    /**
     * 暂停下载音频文件
     *
     * @param musicBean  音频信息
     */
    void pauseDownloadMusicFile(MusicBean musicBean);

    /**
     * 下载音频文件暂停回调
     *
     * @param musicBean  音频信息
     */
    void musicFileLoadingPausedCallback(MusicBean musicBean);

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     */
    void deleteMusicFile(MusicBean musicBean);

    /**
     * 删除音频文件成功回调
     *
     * @param musicBean  音频信息
     */
    void musicFileDeletedCallback(MusicBean musicBean);

    /**
     * 删除音频文件失败回调
     *
     * @param musicBean 音频信息
     */
    void musicFileDeletedFailedCallback(MusicBean musicBean);

    /**
     * panel销毁回调
     */
    void panelOnDestroy();

    /**
     * 播放音乐
     *
     * @param musicBean 音频信息
     */
    void playMusic(MusicBean musicBean);

    /**
     * 暂停音乐
     */
    void pauseMusic();

    /**
     * 重播音乐
     */
    void restartMusic();

    /**
     * 重置播放器
     */
    void resetPlayer();

    /**
     * 停止播放器
     */
    void stopPlayer();

}
