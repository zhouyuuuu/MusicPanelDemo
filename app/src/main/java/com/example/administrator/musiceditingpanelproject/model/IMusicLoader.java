package com.example.administrator.musiceditingpanelproject.model;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;

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
     * @param musicBean  音频信息
     */
    void loadMusicFileData(MusicBean musicBean);

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     */
    void deleteMusicFile(MusicBean musicBean);
}
