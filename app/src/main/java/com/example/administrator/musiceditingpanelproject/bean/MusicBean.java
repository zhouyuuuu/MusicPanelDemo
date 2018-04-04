package com.example.administrator.musiceditingpanelproject.bean;

import java.io.Serializable;

/**
 * Edited by Administrator on 2018/3/24.
 */

public class MusicBean implements Serializable {
    // 下载暂停
    public static final int STATE_DOWNLOAD_PAUSED = 3;
    // 播放
    public static final int STATE_PLAYING = 4;
    // 暂停播放
    public static final int STATE_PLAYING_PAUSED = 5;
    // 下载中
    public static final int STATE_DOWNLOADING = 1;
    // 已下载
    public static final int STATE_DOWNLOADED = 2;
    // 未下载
    public static final int STATE_UNDOWNLOADED = 0;
    // 音频文件url
    private String url;
    // 作者名
    private String authorName;
    // 音频时长
    private String time;
    // 音频版本
    private String version;
    // 最小可见应用版本
    private String minVisibleVersion;
    // 最大可见应用版本
    private String maxVisibleVersion;
    // 音频名称
    private String name;
    // 音频序号
    private String id;
    // 状态
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMinVisibleVersion() {
        return minVisibleVersion;
    }

    public void setMinVisibleVersion(String minVisibleVersion) {
        this.minVisibleVersion = minVisibleVersion;
    }

    public String getMaxVisibleVersion() {
        return maxVisibleVersion;
    }

    public void setMaxVisibleVersion(String maxVisibleVersion) {
        this.maxVisibleVersion = maxVisibleVersion;
    }

    @Override
    public int hashCode() {
        if (url == null) {
            return super.hashCode();
        }
        return url.hashCode();
    }

    /**
     * 我们认为name和url相同的musicBean就是相等的musicBean，因为其下载的音乐文件是同一个
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof MusicBean) {
            String anotherUrl = ((MusicBean) obj).getUrl();
            String anotherName = ((MusicBean) obj).getName();
            return url.equals(anotherUrl) && name.equals(anotherName);
        }
        return false;
    }
}
