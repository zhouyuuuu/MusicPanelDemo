package com.example.administrator.musiceditingpanelproject.model;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;

/**
 * Edited by Administrator on 2018/3/27.
 */

public interface IMusicPlayer {
    void playMusic(MusicBean musicBean);
    void stopMusic();
    void replayMusic();
}
