package com.example.administrator.musiceditingpanelproject.module.editmusic.model;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.module.editmusic.presenter.IMusicManager;

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
     * @param musicBean 音频信息
     */
    void loadMusicFileData(MusicBean musicBean);

    /**
     * 删除音频文件
     *
     * @param musicBean 音频信息
     */
    void deleteMusicFile(MusicBean musicBean);

    /**
     * 暂停加载
     *
     * @param musicBean 音频信息
     */
    void pauseLoading(MusicBean musicBean);

    /**
     * 注册观察者
     *
     * @param iMusicManager 管理器
     */
    void registerMusicManager(IMusicManager iMusicManager);

    /**
     * 取消注册观察者
     *
     * @param iMusicManager 管理器
     */
    void unregisterMusicManager(IMusicManager iMusicManager);
}
