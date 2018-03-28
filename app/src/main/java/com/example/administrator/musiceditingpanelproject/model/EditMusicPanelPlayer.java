package com.example.administrator.musiceditingpanelproject.model;

import android.media.MediaPlayer;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.util.StoreUtil;

import java.io.IOException;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelPlayer implements IMusicPlayer {

    private MediaPlayer mMediaPlayer;

    public EditMusicPanelPlayer() {
        this.mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.reset();
            }
        });
    }

    /**
     * 播放音乐，这边AsyncPlayer已实现异步播放
     *
     * @param musicBean 音频信息
     */
    @Override
    public void playMusic(MusicBean musicBean) {
        String cachePath = StoreUtil.getCacheFileAbsolutePath(musicBean.getVersion(), StoreUtil.getFileName(musicBean.getUrl()));
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(cachePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mMediaPlayer.prepareAsync();
    }

    /**
     * 暂停音乐
     */
    @Override
    public void pauseMusic() {
        mMediaPlayer.pause();
    }

    /**
     * 继续音乐
     */
    @Override
    public void restartMusic() {
        mMediaPlayer.start();
    }

    /**
     * 停止播放器
     */
    @Override
    public void stopPlayer() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }
}
