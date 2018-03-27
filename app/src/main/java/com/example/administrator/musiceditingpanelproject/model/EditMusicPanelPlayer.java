package com.example.administrator.musiceditingpanelproject.model;

import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;

import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.util.StoreUtil;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelPlayer implements IMusicPlayer {

    // TAG
    private static final String TAG = "AsyncPlayer";
    // 异步播放器
    private AsyncPlayer mAsyncPlayer;
    // 当前播放的Uri
    private Uri mCurrentUri = null;

    public EditMusicPanelPlayer() {
        this.mAsyncPlayer = new AsyncPlayer(TAG);
    }

    /**
     * 播放音乐，这边AsyncPlayer已实现异步播放
     *
     * @param musicBean 音频信息
     */
    @Override
    public void playMusic(MusicBean musicBean) {
        String cachePath = StoreUtil.getCacheFileAbsolutePath(musicBean.getVersion(), StoreUtil.getFileName(musicBean.getUrl()));
        mCurrentUri = Uri.parse(cachePath);
        mAsyncPlayer.play(MusicEditingPanelApplication.getApplication(), mCurrentUri, false, AudioManager.STREAM_MUSIC);
    }

    /**
     * 停止音乐
     */
    @Override
    public void stopMusic() {
        mAsyncPlayer.stop();
    }

    /**
     * 重播音乐
     */
    @Override
    public void replayMusic() {
        if (mCurrentUri != null) {
            mAsyncPlayer.play(MusicEditingPanelApplication.getApplication(), mCurrentUri, false, AudioManager.STREAM_MUSIC);
        }
    }
}
