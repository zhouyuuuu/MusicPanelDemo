package com.example.administrator.musiceditingpanelproject.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/24.
 */

public class MusicGroup implements Serializable {
    // 分类名
    private String sortName;
    // 音频信息列表
    private ArrayList<MusicBean> musicBeans;

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public ArrayList<MusicBean> getMusicBeans() {
        return musicBeans;
    }

    public void setMusicBeans(ArrayList<MusicBean> musicBeans) {
        this.musicBeans = musicBeans;
    }
}
