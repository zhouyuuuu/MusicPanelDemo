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

    private static final String TAG = "AsyncPlayer";
    private AsyncPlayer mAsyncPlayer;
    private Uri mCurrentUri = null;

    public EditMusicPanelPlayer() {
        this.mAsyncPlayer = new AsyncPlayer(TAG);
    }

    @Override
    public void playMusic(MusicBean musicBean) {
        String cachePath = StoreUtil.getCacheFileAbsolutePath(musicBean.getVersion(), StoreUtil.getFileName(musicBean.getUrl()));
        mCurrentUri = Uri.parse(cachePath);
        mAsyncPlayer.play(MusicEditingPanelApplication.getApplication(), mCurrentUri ,false, AudioManager.STREAM_MUSIC);
    }

    @Override
    public void stopMusic() {
        mAsyncPlayer.stop();
    }

    @Override
    public void replayMusic() {
        if (mCurrentUri != null) {
            mAsyncPlayer.play(MusicEditingPanelApplication.getApplication(), mCurrentUri ,false, AudioManager.STREAM_MUSIC);
        }
    }
}
