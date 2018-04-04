package com.example.administrator.musiceditingpanelproject.module.editmusic.model;

import android.media.MediaPlayer;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.common.util.LogUtil;
import com.example.administrator.musiceditingpanelproject.module.editmusic.util.StoreUtil;

import java.io.IOException;
import java.util.HashSet;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelPlayer implements IMusicPlayer {

    // 文件格式
    private static final String MP3 = "mp3";
    private static final String M4A = "m4a";
    private static final String _3GP = "3gp";
    private static final String AMR = "amr";
    private static final String WAV = "wav";
    // 播放状态
    private static final int STATE_PLAYING = 0;
    // 暂停状态
    private static final int STATE_PAUSED = 1;
    // 停止状态
    private static final int STATE_STOPPED = 2;
    // 归零状态
    private static final int STATE_IDLE = 3;
    // 结束状态
    private static final int STATE_END = 4;
    // 准备完成状态
    private static final int STATE_PREPARED = 5;
    // 准备中状态
    private static final int STATE_PREPARING = 6;
    private final HashSet<String> formats;
    // 播放器
    private MediaPlayer mMediaPlayer;
    // 当前状态
    private int mState;

    public EditMusicPanelPlayer() {
        this.mMediaPlayer = new MediaPlayer();
        // 支持的格式放在set中，播放时看文件格式是否存在于此set
        formats = new HashSet<>();
        formats.add(MP3);
        formats.add(WAV);
        formats.add(_3GP);
        formats.add(AMR);
        formats.add(M4A);
        mState = STATE_IDLE;
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mState = STATE_PREPARED;
                mMediaPlayer.start();
                mState = STATE_PLAYING;
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.start();
                mState = STATE_PLAYING;
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
        if (mState != STATE_IDLE) return;
        if (musicBean == null) return;
        // 网络文件名
        String netFileName = StoreUtil.getNetFileName(musicBean.getUrl());
        // 拿到格式
        String[] strings = netFileName.split("\\.");
        String format = strings[strings.length - 1];
        // 如果格式不符合，则return
        if (!formats.contains(format)) return;
        // 拿到文件绝对路径
        String cachePath = StoreUtil.getCacheFileAbsolutePath(musicBean.getVersion(), netFileName);
        try {
            mMediaPlayer.setDataSource(cachePath);
        } catch (IOException e) {
            LogUtil.e("文件读取出错");
            e.printStackTrace();
            return;
        } catch (IllegalStateException e) {
            LogUtil.e("非法状态，检查MediaPlayer是否在某个状态调用了不属于该状态的方法");
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e) {
            LogUtil.e("文件描述不可用");
            e.printStackTrace();
            return;
        }
        // 异步加载
        mMediaPlayer.prepareAsync();
        // 记录状态
        mState = STATE_PREPARING;
    }

    /**
     * 暂停音乐
     */
    @Override
    public void pauseMusic() {
        if (mState == STATE_PLAYING) {
            mMediaPlayer.pause();
            mState = STATE_PAUSED;
        }
    }

    /**
     * 继续音乐
     */
    @Override
    public void restartMusic() {
        if (mState == STATE_PAUSED) {
            mMediaPlayer.start();
            mState = STATE_PLAYING;
        }
    }

    /**
     * 重置播放器
     */
    @Override
    public void resetPlayer() {
        if (mState == STATE_PLAYING || mState == STATE_PAUSED) {
            mMediaPlayer.stop();
            mState = STATE_STOPPED;
        }
        mMediaPlayer.reset();
        mState = STATE_IDLE;
    }


    /**
     * 停止播放器
     */
    @Override
    public void stopPlayer() {
        resetPlayer();
        mMediaPlayer.release();
        mState = STATE_END;
    }
}
