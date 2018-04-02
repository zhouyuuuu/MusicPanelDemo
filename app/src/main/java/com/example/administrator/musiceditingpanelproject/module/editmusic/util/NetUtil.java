package com.example.administrator.musiceditingpanelproject.module.editmusic.util;

import android.support.annotation.NonNull;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.retrofit.MusicRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.administrator.musiceditingpanelproject.config.AppConfig.EDIT_MUSIC_HTTP_ERROR_CODE_RANGE_ILLEGAL;
import static com.example.administrator.musiceditingpanelproject.config.AppConfig.EDIT_MUSIC_URL_OFFICIAL;
import static com.example.administrator.musiceditingpanelproject.config.AppConfig.EDIT_MUSIC_URL_TEST;

/**
 * 网络工具类，用于获取网络音频列表，获取音频文件byte数组
 * Edited by Administrator on 2018/3/24.
 */

public class NetUtil {


    // 缓存区大小
    private static final int BUFFER_SIZE = 1024;

    /**
     * 获得音频信息组列表
     *
     * @return 音频信息组列表
     */
    public static ArrayList<MusicGroup> getMusicList() {
        ArrayList<MusicGroup> musicGroups = null;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EDIT_MUSIC_URL_OFFICIAL)
                .build();
        MusicRequest musicRequest = retrofit.create(MusicRequest.class);
        Call<ResponseBody> call = musicRequest.getMusicList();
        Response<ResponseBody> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null || !response.isSuccessful()) return null;
        ResponseBody responseBody = response.body();
        if (responseBody == null) return null;
        try {
            musicGroups = parseResponseToMusicGroups(responseBody.string());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return musicGroups;
    }

    /**
     * 分割出网络url中的文件名
     *
     * @param url 网络url
     * @return 文件名
     */
    private static String getNetFileName(@NonNull String url) {
        // “/”为分隔符
        String[] strings = url.split("/");
        if (strings.length == 0) {
            return "";
        }
        // 返回最后一个string
        return strings[strings.length - 1];
    }

    /**
     * 分割出网络url中文件名之前的字符串
     *
     * @param url 网络url
     * @return BaseUrl
     */
    private static String getBaseUrl(@NonNull String url) {
        StringBuilder stringBuilder = new StringBuilder(url);
        String filename = getNetFileName(url);
        stringBuilder.delete(url.length() - filename.length(), url.length());
        return stringBuilder.toString();
    }

    /**
     * 加载音频文件，这边是边下载边存储，通过RandomAccessFile来断点续传，为防止意外程序意外中断，需要获取fileLength来作为断点位置
     * 由于使用MappedByteBuffer无法获取准确的fileLength，且MappedByteBuffer提升的写入速率在下载总耗时中所占的比例很小，提升效果不明显，所以只使用RandomAccessFile即可
     *
     * @param musicBean 音频信息
     * @return 是否下载成功
     */
    public static boolean downloadMusicFile(MusicBean musicBean, AtomicBoolean isPaused) {
        if (musicBean == null) return false;
        String filename = getNetFileName(musicBean.getUrl());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl(musicBean.getUrl()))
                .build();
        MusicRequest musicRequest = retrofit.create(MusicRequest.class);
        // 先用临时文件名，来区分下载中文件和下载完成文件
        String tempFilePath = StoreUtil.getTempCacheFileAbsolutePath(musicBean.getVersion(), filename);
        File tempFile = new File(tempFilePath);
        // 若没有此文件夹，则创建文件夹
        File cacheFolderPath = new File(StoreUtil.getCacheFolderDir());
        if (!cacheFolderPath.exists()){
            cacheFolderPath.mkdir();
        }
        // 若没有此文件夹，则创建文件夹
        File musicFileCacheFolderPath = new File(StoreUtil.getCacheMusicFileFolderDir());
        if (!musicFileCacheFolderPath.exists()){
            musicFileCacheFolderPath.mkdir();
        }
        Call<ResponseBody> call;
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            randomAccessFile = new RandomAccessFile(tempFile, "rw");
            // 从tempFile.length()位置开始写
            randomAccessFile.seek(tempFile.length());
            // 这里是http协议头 range:bytes= xxx- , xxx为开始访问的位置
            String range = "bytes=" + tempFile.length() + "-";
            // 从range开始获取
            call = musicRequest.getMusicFileWithRange(range, filename);
            // 开始下载前检查是否暂停
            if (isPaused.get()) return false;
            Response<ResponseBody> response = call.execute();
            if (response == null) return false;
            // 如果是416，则说明range越界了，原因是文件已经下载好了，只是没有改名误认为没下载好
            if (response.code() == EDIT_MUSIC_HTTP_ERROR_CODE_RANGE_ILLEGAL) {
                // 改名
                return renameCacheFile(tempFile, StoreUtil.getCacheFileAbsolutePath(musicBean.getVersion(), filename));
            }
            if (!response.isSuccessful()) return false;
            ResponseBody responseBody = response.body();
            if (responseBody == null) return false;
            byte[] buffer = new byte[BUFFER_SIZE];
            InputStream inputStream = responseBody.byteStream();
            bufferedInputStream = new BufferedInputStream(inputStream);
            int len;
            // 循环过程检测是否暂停，否则边下载边写入文件
            while (!isPaused.get() && (len = bufferedInputStream.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, len);
            }
            // 如果是暂停，返回false，如果不是暂停，判断是否重命名成功，成功返回true，否则false，改名用于区分下载中的文件和下载完成的文件
            return !isPaused.get() && renameCacheFile(tempFile, StoreUtil.getCacheFileAbsolutePath(musicBean.getVersion(), filename));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 重命名文件
     * @param file 文件
     * @param rename 名
     * @return 成功失败
     */
    private static boolean renameCacheFile(@NonNull File file, @NonNull String rename) {
        File renameFile = new File(rename);
        if (!renameFile.exists()) {
            file.renameTo(renameFile);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 把Json解析成为音频信息组列表，这个是根据文档提供的格式来解析
     *
     * @param response 服务器返回音频列表的Json的String
     * @return 音频信息组列表
     */
    private static ArrayList<MusicGroup> parseResponseToMusicGroups(String response) {
        ArrayList<MusicGroup> musicGroups = new ArrayList<>();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            jsonObject = jsonObject.getJSONObject("data");
            JSONArray jsonArray = jsonObject.getJSONArray("groupData");
            for (int i = 0; i < jsonArray.length(); i++) {
                MusicGroup musicGroup = new MusicGroup();
                jsonObject = jsonArray.getJSONObject(i);
                musicGroup.setSortName(jsonObject.getString("groupType"));
                JSONArray musicBeanJsonArray = jsonObject.getJSONArray("items");
                ArrayList<MusicBean> musicBeans = new ArrayList<>();
                MusicBean musicBean;
                for (int j = 0; j < musicBeanJsonArray.length(); j++) {
                    JSONObject musicBeanJsonObject = musicBeanJsonArray.getJSONObject(j);
                    musicBean = new MusicBean();
                    musicBean.setUrl(musicBeanJsonObject.getString("zipUrl"));
                    musicBean.setMaxVisibleVersion(musicBeanJsonObject.getString("visibleMaxversion"));
                    musicBean.setMinVisibleVersion(musicBeanJsonObject.getString("visibleMinversion"));
                    musicBean.setName(musicBeanJsonObject.getString("name"));
                    musicBean.setId(musicBeanJsonObject.getString("customId"));
                    musicBean.setVersion(musicBeanJsonObject.getString("version"));
                    musicBeanJsonObject = musicBeanJsonObject.getJSONObject("ext");
                    musicBean.setAuthorName(musicBeanJsonObject.getString("author"));
                    musicBean.setTime(musicBeanJsonObject.getString("totalTime"));
                    musicBeans.add(musicBean);
                }
                musicGroup.setMusicBeans(musicBeans);
                musicGroups.add(musicGroup);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musicGroups;
    }

    /**
     * 解析目前服务器返回的假数据
     *
     * @param response 服务器返回音频列表的Json的String
     * @return 音频信息组列表
     */
    private static ArrayList<MusicGroup> parseResponseToMusicGroupsForTesting(String response) {
        ArrayList<MusicGroup> musicGroups = new ArrayList<>();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            jsonObject = jsonObject.getJSONObject("data");
            MusicGroup musicGroup = new MusicGroup();
            musicGroup.setSortName(jsonObject.getString("groupType"));
            JSONArray musicBeanJsonArray = jsonObject.getJSONArray("items");
            ArrayList<MusicBean> musicBeans = new ArrayList<>();
            MusicBean musicBean;
            for (int j = 0; j < musicBeanJsonArray.length(); j++) {
                JSONObject musicBeanJsonObject = musicBeanJsonArray.getJSONObject(j);
                musicBean = new MusicBean();
                musicBean.setUrl(musicBeanJsonObject.getString("zip_url"));
                musicBean.setMaxVisibleVersion(musicBeanJsonObject.getString("visible_maxversion"));
                musicBean.setMinVisibleVersion(musicBeanJsonObject.getString("visible_minversion"));
                musicBean.setName(musicBeanJsonObject.getString("name"));
                musicBean.setId(musicBeanJsonObject.getString("custom_id"));
                musicBean.setVersion(musicBeanJsonObject.getString("version"));
                musicBeanJsonObject = musicBeanJsonObject.getJSONObject("ext");
                musicBean.setAuthorName(musicBeanJsonObject.getString("author"));
                musicBean.setTime(musicBeanJsonObject.getString("totalTime"));
                musicBeans.add(musicBean);
            }
            musicGroup.setMusicBeans(musicBeans);
            musicGroups.add(musicGroup);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musicGroups;
    }


}
