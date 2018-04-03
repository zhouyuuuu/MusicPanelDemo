package com.example.administrator.musiceditingpanelproject.module.editmusic.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Retrofit接口，获取音频数据
 * Edited by Administrator on 2018/3/24.
 */

public interface MusicRequest {

    /**
     * 获取音频信息组列表
     *
     * @return ResponseBody
     */
    @Headers({"lang:Chinese", "platform:android", "version:1.13.0", "deviceid:imei"})
    @GET("materials?type=music")
    Call<ResponseBody> getMusicList();

    /**
     * 断点续传获取音频文件
     *
     * @param filename 文件名
     * @return ResponseBody
     */
    @Headers({"lang:Chinese", "platform:android", "version:1.13.0", "deviceid:imei"})
    @Streaming
    @GET("{filename}")
    Call<ResponseBody> getMusicFileWithRange(@Header("Range") String range, @Path("filename") String filename);


}
