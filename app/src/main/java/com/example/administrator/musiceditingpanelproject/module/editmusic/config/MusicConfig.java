package com.example.administrator.musiceditingpanelproject.module.editmusic.config;

/**
 * Edited by Administrator on 2018/4/2.
 */

public class MusicConfig {
    // 测试环境host
    public static final String EDIT_MUSIC_URL_TEST = "http://dev.mixvvideo.com/";
    // 正式环境host
    public static final String EDIT_MUSIC_URL_OFFICIAL = "https://www.mixvvideo.com/";
    // range超出返回错误代码
    public static final int EDIT_MUSIC_HTTP_ERROR_CODE_RANGE_ILLEGAL = 416;
    // 分隔符
    public static final String EDIT_MUSIC_CACHE_FILE_NAME_DELIMITER = "@#";
    // 缓存文件夹名
    public static final String EDIT_MUSIC_CACHE_FOLDER = "/MixVDownload";
    // 缓存音乐文件文件夹名
    public static final String EDIT_MUSIC_CACHE_MUSIC_FILE_FOLDER = "/Music";
    // 缓存音乐列表文件夹名
    public static final String EDIT_MUSIC_CACHE_LIST_FOLDER = "/MusicListCache";
    // 缓存音乐列表文件名
    public static final String EDIT_MUSIC_CACHE_LIST_FILE = "/ListCache";
    // 临时文件后缀
    public static final String EDIT_MUSIC_TEMP_FILE_SUFFIX = ".temp";

}
