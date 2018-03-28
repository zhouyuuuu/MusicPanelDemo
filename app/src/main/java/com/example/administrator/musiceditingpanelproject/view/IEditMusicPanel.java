package com.example.administrator.musiceditingpanelproject.view;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.util.ArrayList;

/**
 * 音频编辑面板接口
 * Edited by Administrator on 2018/3/27.
 */

public interface IEditMusicPanel {
    /**
     * musicBean的状态改变了，同步回调
     *
     * @param musicBean 音频信息
     */
    void musicBeanStateChangedCallback(MusicBean musicBean);

    /**
     * 音频信息分组列表加载成功，这个方法会被异步回调
     *
     * @param musicGroups 音频信息分组列表
     */
    void musicGroupListLoadedCallback(ArrayList<MusicGroup> musicGroups);

    /**
     * 音频信息分组列表加载失败，这个方法会被异步回调
     */
    void musicGroupListDataLoadedFailedCallback();

    /**
     * 音乐文件下载成功回调，同步
     *
     * @param musicBean  音频信息
     */
    void musicFileDataLoadedCallback(MusicBean musicBean);

    /**
     * 音乐文件下载失败回调，同步
     *
     * @param musicBean 音频信息
     */
    void musicFileDataLoadedFailedCallback(MusicBean musicBean);

    /**
     * 音乐文件删除成功回调，同步
     *
     * @param musicBean 音频信息
     */
    void musicFileDataDeletedCallback(MusicBean musicBean);

    /**
     * 音乐文件删除失败回调，同步
     *
     * @param musicBean 音频信息
     */
    void musicFileDataDeletedFailedCallback(MusicBean musicBean);
}
