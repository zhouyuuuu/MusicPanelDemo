package com.example.administrator.musiceditingpanelproject.model;

import android.support.annotation.NonNull;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.presenter.IMusicManager;
import com.example.administrator.musiceditingpanelproject.util.NetUtil;
import com.example.administrator.musiceditingpanelproject.util.StoreUtil;
import com.example.administrator.musiceditingpanelproject.util.VersionUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Edited by Administrator on 2018/3/27.
 */

public class EditMusicPanelLoader implements IMusicLoader {

    // 默认线程等级
    private static final int PRIORITY_DEFAULT = 1;
    // 线程池
    private ThreadPoolExecutor mThreadPoolExecutor;
    // 管理器的弱引用
    private WeakReference<IMusicManager> mIMusicManagerWeakReference;
    // 是否有已提交的列表任务
    private volatile AtomicBoolean isLoadingMusicGroupList;
    // 已提交的文件任务
    private final HashSet<MusicBean> mLoadingMusicBeanSet;
    // 已提交的删除任务
    private final HashSet<MusicBean> mDeletingMusicBeanSet;

    public EditMusicPanelLoader(IMusicManager iMusicManager) {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int NUMBER_OF_MAX = 2 * NUMBER_OF_CORES;
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_MAX, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        mIMusicManagerWeakReference = new WeakReference<>(iMusicManager);
        isLoadingMusicGroupList = new AtomicBoolean(false);
        mLoadingMusicBeanSet = new HashSet<>();
        mDeletingMusicBeanSet = new HashSet<>();
    }

    /**
     * 过滤不可见音频信息
     *
     * @param musicGroups 音频信息分组列表
     */
    private static void filterInvisibleMusicBean(ArrayList<MusicGroup> musicGroups) {
        for (MusicGroup musicGroup : musicGroups) {
            ArrayList<MusicBean> musicBeans = musicGroup.getMusicBeans();
            for (int i = musicBeans.size() - 1; i >= 0; i--) {
                MusicBean musicBean = musicBeans.get(i);
                if (!VersionUtil.versionIsMatch(musicBean.getMinVisibleVersion(), musicBean.getMaxVisibleVersion())) {
                    musicBeans.remove(i);
                }
            }
        }
    }

    /**
     * 检查音频信息分组列表中音频信息的状态，如音频信息存在缓存，则将state改为已下载
     *
     * @param musicGroups 音频信息分组列表
     */
    private static void checkMusicBeanState(ArrayList<MusicGroup> musicGroups) {
        HashSet<String> mCacheFileNameSet = StoreUtil.getAllCacheFileName();
        if (mCacheFileNameSet == null) return;
        for (MusicGroup musicGroup : musicGroups) {
            for (MusicBean musicBean : musicGroup.getMusicBeans()) {
                if (mCacheFileNameSet.contains(StoreUtil.getCacheFilename(musicBean.getVersion(), StoreUtil.getFileName(musicBean.getUrl())))) {
                    musicBean.setState(MusicBean.STATE_DOWNLOADED);
                } else {
                    musicBean.setState(MusicBean.STATE_UNDOWNLOADED);
                }
            }
        }
    }

    /**
     * 假数据
     */
    private static ArrayList<MusicGroup> createFalseData() {
        ArrayList<MusicGroup> musicGroups = new ArrayList<>();
        musicGroups.add(createFalseMusicGroup("iTunes", 6));
        musicGroups.add(createFalseMusicGroup("欢乐", 9));
        musicGroups.add(createFalseMusicGroup("节奏", 10));
        musicGroups.add(createFalseMusicGroup("电影感", 16));
        musicGroups.add(createFalseMusicGroup("轻音乐", 3));
        return musicGroups;
    }

    /**
     * 假音频分组
     */
    private static MusicGroup createFalseMusicGroup(String type, int num) {
        MusicGroup musicGroup = new MusicGroup();
        musicGroup.setSortName(type);
        ArrayList<MusicBean> musicBeans = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            MusicBean musicBean = new MusicBean();
            musicBean.setTime("2000");
            musicBean.setVersion("1.1");
            musicBean.setAuthorName("高橋優");
            musicBean.setId(String.valueOf(i));
            musicBean.setName("ヤキモチ");
            musicBean.setMinVisibleVersion("1.2");
            musicBean.setMaxVisibleVersion("1.4");
            musicBean.setUrl("http://dldir1.qq.com/weixin/android/weixin665android1280.apk");
            musicBeans.add(musicBean);
        }
        musicGroup.setMusicBeans(musicBeans);
        return musicGroup;
    }

    /**
     * 提交线程加载音频分组列表数据
     */
    @Override
    public void loadMusicGroupListData() {
        if (isLoadingMusicGroupList.get()) return;
        isLoadingMusicGroupList.compareAndSet(false,true);
        mThreadPoolExecutor.execute(new LoadMusicGroupListRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference,isLoadingMusicGroupList));
    }

    /**
     * 提交线程加载音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void loadMusicFileData(MusicBean musicBean) {
        synchronized (mLoadingMusicBeanSet) {
            if (mLoadingMusicBeanSet.contains(musicBean)) return;
            mLoadingMusicBeanSet.add(musicBean);
        }
        mThreadPoolExecutor.execute(new LoadMusicFileRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, musicBean, mLoadingMusicBeanSet));
    }

    /**
     * 提交线程删除音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void deleteMusicFile(MusicBean musicBean) {
        synchronized (mDeletingMusicBeanSet) {
            if (mDeletingMusicBeanSet.contains(musicBean)) return;
            mDeletingMusicBeanSet.add(musicBean);
        }
        mThreadPoolExecutor.execute(new DeleteMusicFileRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, musicBean, mDeletingMusicBeanSet));
    }

    @Override
    public void stopLoading() {
        if (mThreadPoolExecutor!=null){
            mThreadPoolExecutor.shutdownNow();
        }
    }

    /**
     * 加载音频信息分组列表的任务类
     */
    private static class LoadMusicGroupListRunnable extends BaseLoadRunnable {

        AtomicBoolean isLoadingMusicGroupList;

        LoadMusicGroupListRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, AtomicBoolean isLoadingMusicGroupList) {
            super(priority, iMusicManagerWeakReference);
            this.isLoadingMusicGroupList = isLoadingMusicGroupList;
        }

        @Override
        void call() {
            // 获取网络列表
//            ArrayList<MusicGroup> musicGroups = NetUtil.getMusicList();
            ArrayList<MusicGroup> musicGroups = createFalseData();
            if (musicGroups == null) {
                // 网络列表获取不到则读缓存
                musicGroups = StoreUtil.readMusicList();
                if (musicGroups == null) {
                    // 加载不到列表信息，回调加载失败
                    IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                    if (iMusicManager != null) {
                        iMusicManager.musicGroupListDataLoadedFailedCallback();
                    }
                    isLoadingMusicGroupList.compareAndSet(true,false);
                    return;
                }
            }
            // 过滤版本不可见的MusicBean
            filterInvisibleMusicBean(musicGroups);
            // 检查MusicBean的状态（是否已下载）
            checkMusicBeanState(musicGroups);
            // 回调加载成功
            IMusicManager iMusicManager = iMusicManagerWeakReference.get();
            if (iMusicManager != null) {
                iMusicManager.musicGroupListDataLoadedCallback(musicGroups);
            }
            // 缓存列表信息
            StoreUtil.cacheMusicList(musicGroups);
            // 清理掉不存在于列表中的音乐文件
            StoreUtil.sortOutCache(musicGroups);
            isLoadingMusicGroupList.compareAndSet(true,false);
        }
    }

    /**
     * 加载音频文件的任务类
     */
    private static class LoadMusicFileRunnable extends BaseLoadRunnable {

        MusicBean mMusicBean;
        final HashSet<MusicBean> mLoadingMusicBeanSet;

        LoadMusicFileRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, MusicBean musicBean, HashSet<MusicBean> loadingMusicBeanSet) {
            super(priority, iMusicManagerWeakReference);
            this.mMusicBean = musicBean;
            this.mLoadingMusicBeanSet = loadingMusicBeanSet;
        }

        @Override
        void call() {
            // 要下载的文件是否已经存在
            boolean exist = StoreUtil.findCacheFile(mMusicBean.getVersion(), StoreUtil.getFileName(mMusicBean.getUrl())) != null;
            if (exist) {
                // 回调加载成功
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                iMusicManager.musicFileDataLoadedCallback(mMusicBean);
                synchronized (mLoadingMusicBeanSet){
                    mLoadingMusicBeanSet.remove(mMusicBean);
                }
                return;
            }
            // 加载网络文件数据
            boolean isSuccessful = NetUtil.loadMusicFile(mMusicBean);
            // 如果数据为空，或者存储文件失败
            if (!isSuccessful) {
                // 回调下载失败
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileDataLoadedFailedCallback(mMusicBean);
            } else {
                // 回调下载成功
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileDataLoadedCallback(mMusicBean);
            }
            synchronized (mLoadingMusicBeanSet){
                mLoadingMusicBeanSet.remove(mMusicBean);
            }
        }
    }

    /**
     * 删除音频文件的任务类
     */
    private static class DeleteMusicFileRunnable extends BaseLoadRunnable {

        MusicBean musicBean;
        final HashSet<MusicBean> mDeletingMusicBeanSet;

        DeleteMusicFileRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, MusicBean musicBean, HashSet<MusicBean> deletingMusicBeanSet) {
            super(priority, iMusicManagerWeakReference);
            this.musicBean = musicBean;
            this.mDeletingMusicBeanSet = deletingMusicBeanSet;
        }

        @Override
        void call() {
            // 是否删除成功
            boolean deleted = StoreUtil.deleteCache(musicBean.getVersion(), StoreUtil.getFileName(musicBean.getUrl()));
            if (deleted) {
                // 回调删除成功
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileDataDeletedCallback(musicBean);
            } else {
                // 回掉删除失败
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileDataDeletedFailedCallback(musicBean);
            }
            synchronized (mDeletingMusicBeanSet){
                mDeletingMusicBeanSet.remove(musicBean);
            }
        }
    }

    /**
     * 线程池运行Runnable的基类
     */
    private static abstract class BaseLoadRunnable implements Runnable, Comparable<BaseLoadRunnable> {

        WeakReference<IMusicManager> iMusicManagerWeakReference;
        private int priority;

        BaseLoadRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference) {
            this.priority = priority;
            this.iMusicManagerWeakReference = iMusicManagerWeakReference;
        }

        private int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(@NonNull BaseLoadRunnable another) {
            int my = this.getPriority();
            int other = another.getPriority();
            return my < other ? 1 : my > other ? -1 : 0;
        }

        @Override
        public void run() {
            call();
        }

        abstract void call();
    }
}
