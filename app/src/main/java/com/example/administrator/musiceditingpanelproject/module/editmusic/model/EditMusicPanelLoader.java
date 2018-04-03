package com.example.administrator.musiceditingpanelproject.module.editmusic.model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.module.editmusic.presenter.IMusicManager;
import com.example.administrator.musiceditingpanelproject.module.editmusic.util.NetUtil;
import com.example.administrator.musiceditingpanelproject.module.editmusic.util.StoreUtil;
import com.example.administrator.musiceditingpanelproject.common.util.VersionUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final AtomicBoolean mIsLoadingMusicGroupList;
    // 已提交的文件任务
    private final HashMap<MusicBean,LoadMusicFileRunnable> mDownloadingTaskMap;
    // 已提交的删除任务
    private final HashSet<MusicBean> mDeletingTaskSet;

    public EditMusicPanelLoader(IMusicManager iMusicManager) {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int NUMBER_OF_MAX = 2 * NUMBER_OF_CORES;
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_MAX, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        mIMusicManagerWeakReference = new WeakReference<>(iMusicManager);
        mIsLoadingMusicGroupList = new AtomicBoolean(false);
        mDownloadingTaskMap = new HashMap<>();
        mDeletingTaskSet = new HashSet<>();
    }

    /**
     * 过滤不可见音频信息,将版本大于最大版本、小于最小版本的删除
     *
     * @param musicGroups 音频信息分组列表
     */
    private static void filterInvisibleMusicBean(@NonNull ArrayList<MusicGroup> musicGroups) {
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
     * 检查音频信息分组列表中音频信息的状态，如音频信息存在缓存，则将state改为已下载，如果存在临时文件，则将state改为暂停下载
     *
     * @param musicGroups 音频信息分组列表
     */
    private static void checkMusicBeanState(@NonNull ArrayList<MusicGroup> musicGroups) {
        HashSet<String> mCacheFileNameSet = StoreUtil.getAllCacheFileName();
        if (mCacheFileNameSet == null) return;
        for (MusicGroup musicGroup : musicGroups) {
            for (MusicBean musicBean : musicGroup.getMusicBeans()) {
                if (mCacheFileNameSet.contains(StoreUtil.getCacheFileName(musicBean.getVersion(), StoreUtil.getNetFileName(musicBean.getUrl())))) {
                    musicBean.setState(MusicBean.STATE_DOWNLOADED);
                }else if (mCacheFileNameSet.contains(StoreUtil.getTempCacheFileName(musicBean.getVersion(), StoreUtil.getNetFileName(musicBean.getUrl())))){
                    musicBean.setState(MusicBean.STATE_DOWNLOAD_PAUSED);
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
        synchronized (mIsLoadingMusicGroupList) {
            // 有正处理的请求，直接返回，防止重复任务
            if (mIsLoadingMusicGroupList.get()) return;
            // 标志设为正在请求列表信息
            mIsLoadingMusicGroupList.compareAndSet(false, true);
        }
        // 提交线程池
        mThreadPoolExecutor.execute(new LoadMusicGroupListRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, mIsLoadingMusicGroupList));
    }

    /**
     * 提交线程加载音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void loadMusicFileData(@NonNull MusicBean musicBean) {
        // 新的任务
        LoadMusicFileRunnable loadMusicFileRunnable = new LoadMusicFileRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, musicBean, mDownloadingTaskMap);
        synchronized (mDownloadingTaskMap) {
            // 已存在任务则返回，防重
            if (mDownloadingTaskMap.containsKey(musicBean)) return;
            // 注册任务
            mDownloadingTaskMap.put(musicBean,loadMusicFileRunnable);
        }
        // 提交线程池
        mThreadPoolExecutor.execute(loadMusicFileRunnable);
    }

    /**
     * 提交线程删除音频文件
     *
     * @param musicBean  音频信息
     */
    @Override
    public void deleteMusicFile(@NonNull MusicBean musicBean) {
        synchronized (mDeletingTaskSet) {
            // 已存在任务则返回，防重
            if (mDeletingTaskSet.contains(musicBean)) return;
            // 注册任务
            mDeletingTaskSet.add(musicBean);
        }
        // 提交线程池
        mThreadPoolExecutor.execute(new DeleteMusicFileRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, musicBean, mDeletingTaskSet));
    }

    @Override
    public void pauseLoading(@NonNull MusicBean musicBean) {
        // 通过musicBean拿到任务
        LoadMusicFileRunnable loadMusicFileRunnable = mDownloadingTaskMap.get(musicBean);
        // 拿不到就返回了
        if (loadMusicFileRunnable == null) return;
        // 暂停
        loadMusicFileRunnable.setIsPaused();
    }

    @Override
    public void stopLoading() {
        // 终结线程池
        if (mThreadPoolExecutor!=null){
            mThreadPoolExecutor.shutdownNow();
        }
        synchronized (mDownloadingTaskMap){
            for (LoadMusicFileRunnable loadMusicFileRunnable : mDownloadingTaskMap.values()) {
                loadMusicFileRunnable.setIsPaused();
            }
        }
    }

    /**
     * 加载音频信息分组列表的任务类
     */
    private static class LoadMusicGroupListRunnable extends BaseLoadRunnable {

        // 原子标志位
        final AtomicBoolean mIsLoadingMusicGroupList;

        LoadMusicGroupListRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, AtomicBoolean isLoadingMusicGroupList) {
            super(priority, iMusicManagerWeakReference);
            this.mIsLoadingMusicGroupList = isLoadingMusicGroupList;
        }

        @Override
        void call() {
            // 获取网络列表
//            ArrayList<MusicGroup> musicGroups = NetUtil.getMusicList();
            ArrayList<MusicGroup> musicGroups = createFalseData();
            if (musicGroups == null) {
                // 网络列表获取不到则读缓存
                musicGroups = StoreUtil.readCacheMusicList();
                if (musicGroups == null) {
                    // 加载不到列表信息，回调加载失败
                    IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                    if (iMusicManager != null) {
                        iMusicManager.musicGroupListDataLoadedFailedCallback();
                    }
                    // 设置为没有列表请求
                    mIsLoadingMusicGroupList.compareAndSet(true,false);
                    return;
                }
            }
            // 过滤版本不可见的MusicBean
            filterInvisibleMusicBean(musicGroups);
            // 检查并设置好MusicBean的状态（是否已下载或是下载中）
            checkMusicBeanState(musicGroups);
            // 回调加载成功
            IMusicManager iMusicManager = iMusicManagerWeakReference.get();
            if (iMusicManager != null) {
                iMusicManager.musicGroupListDataLoadedCallback(musicGroups);
            }
            // 缓存列表信息
            StoreUtil.writeCacheMusicList(musicGroups);
            // 清理掉不存在于列表中的音乐文件
            StoreUtil.sortOutCache(musicGroups);
            // 设置为没有列表请求
            synchronized (mIsLoadingMusicGroupList) {
                mIsLoadingMusicGroupList.compareAndSet(true, false);
            }
        }
    }

    /**
     * 加载音频文件的任务类
     */
    private static class LoadMusicFileRunnable extends BaseLoadRunnable {

        private MusicBean mMusicBean;
        // 注册池，记录进行中的下载任务
        private final HashMap<MusicBean,LoadMusicFileRunnable> mDownloadingTaskMap;
        // 原子可见标志位
        private volatile AtomicBoolean mIsPaused;

        LoadMusicFileRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, MusicBean musicBean, HashMap<MusicBean,LoadMusicFileRunnable> downloadingTaskMap) {
            super(priority, iMusicManagerWeakReference);
            this.mMusicBean = musicBean;
            this.mDownloadingTaskMap = downloadingTaskMap;
            mIsPaused = new AtomicBoolean(false);
        }

        @Override
        void call() {
            // 要下载的文件是否已经存在
            boolean exist = StoreUtil.findCacheFile(mMusicBean.getVersion(), StoreUtil.getNetFileName(mMusicBean.getUrl())) != null;
            if (exist) {
                // 回调加载成功
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager != null) {
                    iMusicManager.musicFileLoadedCallback(mMusicBean);
                }
                // 取消注册
                synchronized (mDownloadingTaskMap){
                    mDownloadingTaskMap.remove(mMusicBean);
                }
                return;
            }
            // 加载网络文件数据
            boolean isSuccessful = NetUtil.downloadMusicFile(mMusicBean, mIsPaused);
            // 如果数据为空，或者存储文件失败
            if (!isSuccessful) {
                // 回调下载失败
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                if (mIsPaused.get()) {
                    // 如果是被暂停了，回调暂停
                    iMusicManager.musicFileLoadingPausedCallback(mMusicBean);
                } else{
                    // 不是暂停的说明下载出问题了，回调失败
                    iMusicManager.musicFileLoadedFailedCallback(mMusicBean);
                }
            } else {
                // 回调下载成功
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileLoadedCallback(mMusicBean);
            }
            // 取消注册
            synchronized (mDownloadingTaskMap){
                mDownloadingTaskMap.remove(mMusicBean);
            }
        }

        private void setIsPaused(){
            this.mIsPaused.compareAndSet(false, true);
        }
    }

    /**
     * 删除音频文件的任务类
     */
    private static class DeleteMusicFileRunnable extends BaseLoadRunnable {

        MusicBean mMusicBean;
        final HashSet<MusicBean> mDeletingMusicBeanSet;

        DeleteMusicFileRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, MusicBean musicBean, HashSet<MusicBean> deletingMusicBeanSet) {
            super(priority, iMusicManagerWeakReference);
            this.mMusicBean = musicBean;
            this.mDeletingMusicBeanSet = deletingMusicBeanSet;
        }

        @Override
        void call() {
            // 是否删除成功
            boolean deleted = StoreUtil.deleteCacheFile(mMusicBean.getVersion(), StoreUtil.getNetFileName(mMusicBean.getUrl()));
            if (deleted) {
                // 回调删除成功
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileDeletedCallback(mMusicBean);
            } else {
                // 回掉删除失败
                final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                if (iMusicManager == null) return;
                iMusicManager.musicFileDeletedFailedCallback(mMusicBean);
            }
            // 取消注册
            synchronized (mDeletingMusicBeanSet){
                mDeletingMusicBeanSet.remove(mMusicBean);
            }
        }
    }

    /**
     * 线程池运行Runnable的基类
     */
    private static abstract class BaseLoadRunnable implements Runnable, Comparable<BaseLoadRunnable> {

        // 视图层的弱引用
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
