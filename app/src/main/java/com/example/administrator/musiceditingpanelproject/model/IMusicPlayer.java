package com.example.administrator.musiceditingpanelproject.model;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;

/**
 * 音频播放器接口
 * Edited by Administrator on 2018/3/27.
 */

public interface IMusicPlayer {
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
     * 继续音乐
     */
    void restartMusic();

    /**
     * 停止播放器
     */
    void stopPlayer();
}
