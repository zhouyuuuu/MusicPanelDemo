package com.example.administrator.musiceditingpanelproject.presenter;

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
    void refreshMusicEditPanel();

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
    void downloadMusic(MusicBean musicBean);

    /**
     * 下载音频文件成功回调
     *
     * @param musicBean  音频信息
     */
    void musicFileDataLoadedCallback(MusicBean musicBean);

    /**
     * 下载音频文件失败回调
     *
     * @param musicBean  音频信息
     */
    void musicFileDataLoadedFailedCallback(MusicBean musicBean);

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     */
    void deleteMusic(MusicBean musicBean);

    /**
     * 删除音频文件成功回调
     *
     * @param musicBean  音频信息
     */
    void musicFileDataDeletedCallback(MusicBean musicBean);

    /**
     * 删除音频文件失败回调
     *
     * @param musicBean 音频信息
     */
    void musicFileDataDeletedFailedCallback(MusicBean musicBean);

    /**
     * 播放音乐
     *
     * @param musicBean 音频信息
     */
    void playMusic(MusicBean musicBean);

    /**
     * 停止音乐
     */
    void stopMusic();

    /**
     * 重播音乐
     */
    void replayMusic();

}
