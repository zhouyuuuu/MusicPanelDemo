package com.example.administrator.musiceditingpanelproject.presenter;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter.ItemHolder;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.model.EditMusicPanelLoader;
import com.example.administrator.musiceditingpanelproject.model.EditMusicPanelPlayer;
import com.example.administrator.musiceditingpanelproject.model.IMusicLoader;
import com.example.administrator.musiceditingpanelproject.model.IMusicPlayer;
import com.example.administrator.musiceditingpanelproject.util.BindUtil;
import com.example.administrator.musiceditingpanelproject.view.IEditMusicPanel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelManager implements IMusicManager {

    private IMusicLoader iMusicLoader;
    private IMusicPlayer iMusicPlayer;
    private WeakReference<IEditMusicPanel> iEditMusicPanelWeakReference;

    public EditMusicPanelManager(IEditMusicPanel iEditMusicPanel) {
        this.iEditMusicPanelWeakReference = new WeakReference<>(iEditMusicPanel);
        iMusicPlayer = new EditMusicPanelPlayer();
        iMusicLoader = new EditMusicPanelLoader(this);
    }

    /**
     * 刷新音频展示面板，先获取音乐列表数据，获取后会回调面板刷新
     */
    @Override
    public void refreshMusicEditPanel() {
        iMusicLoader.loadMusicGroupListData();
    }

    /**
     * 音频信息分组列表数据加载成功回调
     *
     * @param musicGroups 音频信息分组列表
     */
    @Override
    public void musicGroupListDataLoadedCallback(ArrayList<MusicGroup> musicGroups) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicGroupListLoadedCallback(musicGroups);
    }

    /**
     * 音频信息分组列表数据加载失败回调
     */
    @Override
    public void musicGroupListDataLoadedFailedCallback() {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicGroupListDataLoadedFailedCallback();
    }

    /**
     * 下载音频文件，将View和url绑定起来，将musicBean设为下载中，更新View
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     * @param sort       分类
     * @param page       分页
     * @param position   位置
     */
    @Override
    public void downloadMusic(MusicBean musicBean, ItemHolder itemHolder, String sort, int page, int position) {
        BindUtil.bindUrlAndView(itemHolder.itemView, musicBean.getUrl());
        musicBean.setState(MusicBean.STATE_DOWNLOADING);
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        iEditMusicPanel.musicBeanStateChangedCallback(itemHolder, musicBean);
        iMusicLoader.loadMusicFileData(musicBean, itemHolder, sort, page, position);
    }

    /**
     * 下载音频文件成功回调，判断是否url与View有绑定关系，没有则说明View被别的url重用了，则不通知面板进行更新
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     * @param sort       分类
     * @param page       分页
     * @param position   位置
     */
    @Override
    public void musicFileDataLoadedCallback(MusicBean musicBean, ItemHolder itemHolder, String sort, int page, int position) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        if (BindUtil.isBound(itemHolder.itemView, musicBean.getUrl())) {
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder, musicBean);
            iEditMusicPanel.musicFileDataLoadedCallback(musicBean, itemHolder, sort, page, position);
        }
    }

    /**
     * 下载音频文件失败回调
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     */
    @Override
    public void musicFileDataLoadedFailedCallback(MusicBean musicBean, ItemHolder itemHolder) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        if (BindUtil.isBound(itemHolder.itemView, musicBean.getUrl())) {
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder, musicBean);
        }
        iEditMusicPanel.musicFileDataLoadedFailedCallback(musicBean);
    }

    /**
     * 删除音频文件
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     */
    @Override
    public void deleteMusic(MusicBean musicBean, ItemHolder itemHolder) {
        BindUtil.bindUrlAndView(itemHolder.itemView, musicBean.getUrl());
        iMusicLoader.deleteMusicFile(musicBean, itemHolder);
    }

    /**
     * 删除音频文件成功回调
     *
     * @param musicBean  音频信息
     * @param itemHolder 对应View的Holder
     */
    @Override
    public void musicFileDataDeletedCallback(MusicBean musicBean, ItemHolder itemHolder) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        if (BindUtil.isBound(itemHolder.itemView, musicBean.getUrl())) {
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder, musicBean);
        }
        iEditMusicPanel.musicFileDataDeletedCallback(musicBean);
    }

    /**
     * 删除音频文件失败回调
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicFileDataDeletedFailedCallback(MusicBean musicBean, ItemHolder itemHolder) {
        IEditMusicPanel iEditMusicPanel = iEditMusicPanelWeakReference.get();
        if (iEditMusicPanel == null) return;
        if (BindUtil.isBound(itemHolder.itemView, musicBean.getUrl())) {
            iEditMusicPanel.musicBeanStateChangedCallback(itemHolder, musicBean);
        }
        iEditMusicPanel.musicFileDataDeletedFailedCallback(musicBean);
    }

    /**
     * 播放音乐
     *
     * @param musicBean 音频信息
     */
    @Override
    public void playMusic(MusicBean musicBean) {
        iMusicPlayer.playMusic(musicBean);
    }

    /**
     * 停止音乐
     */
    @Override
    public void stopMusic() {
        iMusicPlayer.stopMusic();
    }

    /**
     * 重播音乐
     */
    @Override
    public void replayMusic() {
        iMusicPlayer.replayMusic();
    }

}
