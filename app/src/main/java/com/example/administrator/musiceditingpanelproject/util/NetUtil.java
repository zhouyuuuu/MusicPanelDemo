package com.example.administrator.musiceditingpanelproject.util;


import android.support.annotation.NonNull;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.retrofit.MusicRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 网络工具类，用于获取网络音频列表，获取音频文件byte数组
 * Edited by Administrator on 2018/3/24.
 */

public class NetUtil {

    // 测试环境host
    private static final String URL_TEST = "http://dev.mixvvideo.com/";
    // 正式环境host
    private static final String URL_OFFICIAL = "https://www.mixvvideo.com/";

    /**
     * 获得音频信息组列表
     *
     * @return 音频信息组列表
     */
    public static ArrayList<MusicGroup> getMusicList() {
        ArrayList<MusicGroup> musicGroups = null;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_TEST)
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
            musicGroups = parseResponseToMusicGroupsForTesting(responseBody.string());
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
    private static String getFileName(@NonNull String url) {
        // “/”为分隔符
        String[] strings = url.split("/");
        if (strings.length == 0) {
            return "";
        }
        // 返回最后一个string
        return strings[strings.length - 1];
    }

    /**
     * 分割出网络url中的BaseUrl
     *
     * @param url 网络url
     * @return BaseUrl
     */
    private static String getBaseUrl(@NonNull String url) {
        StringBuilder stringBuilder = new StringBuilder(url);
        String filename = getFileName(url);
        stringBuilder.delete(url.length()-filename.length(),url.length());
        return stringBuilder.toString();
    }

    /**
     * 加载音频文件byte数组
     *
     * @param musicBean 音频信息
     * @return byte数组
     */
    public static byte[] loadMusicFile(MusicBean musicBean) {
        String filename = getFileName(musicBean.getUrl());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl(musicBean.getUrl()))
                .build();
        MusicRequest musicRequest = retrofit.create(MusicRequest.class);
        Call<ResponseBody> call = musicRequest.getMusicFile(filename);
        Response<ResponseBody> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null || !response.isSuccessful()) return null;
        ResponseBody responseBody = response.body();
        if (responseBody == null) return null;
        byte[] musicByte = null;
        try {
            musicByte = responseBody.bytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return musicByte;
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
