package com.example.administrator.musiceditingpanelproject.module.editmusic.model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.common.util.VersionUtil;
import com.example.administrator.musiceditingpanelproject.module.editmusic.presenter.IMusicManager;
import com.example.administrator.musiceditingpanelproject.module.editmusic.util.NetUtil;
import com.example.administrator.musiceditingpanelproject.module.editmusic.util.StoreUtil;

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
    private final ThreadPoolExecutor mThreadPoolExecutor;
    // 管理器的弱引用
    private final WeakReference<IMusicManager> mIMusicManagerWeakReference;
    // 是否有已提交的列表任务
    private final HashSet<LoadMusicGroupListRunnable> mLoadingListTaskSet;
    // 已提交的文件任务
    private final HashMap<MusicBean, LoadMusicFileRunnable> mDownloadingTaskMap;
    // 已提交的删除任务
    private final HashMap<MusicBean, DeleteMusicFileRunnable> mDeletingTaskMap;
    // 主线程Handler，用来回调
    private final Handler mMainHandler;
    // Handler提交的任务
    private final ArrayList<Runnable> mHandlerRunnables;

    public EditMusicPanelLoader(IMusicManager iMusicManager) {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int NUMBER_OF_MAX = 2 * NUMBER_OF_CORES;
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_MAX, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        mIMusicManagerWeakReference = new WeakReference<>(iMusicManager);
        mLoadingListTaskSet = new HashSet<>(1);
        mDownloadingTaskMap = new HashMap<>();
        mDeletingTaskMap = new HashMap<>();
        mMainHandler = new Handler(Looper.getMainLooper());
        mHandlerRunnables = new ArrayList<>();
    }

    /**
     * 过滤不可见音频信息,将版本大于最大版本、小于最小版本的删除
     *
     * @param musicGroups 音频信息分组列表
     */
    private static void filterInvisibleMusicBean(@NonNull ArrayList<MusicGroup> musicGroups) {
        ArrayList<MusicGroup> removes = new ArrayList<>();
        for (MusicGroup musicGroup : musicGroups) {
            ArrayList<MusicBean> musicBeans = musicGroup.getMusicBeans();
            for (int i = musicBeans.size() - 1; i >= 0; i--) {
                MusicBean musicBean = musicBeans.get(i);
                if (!VersionUtil.versionIsMatch(musicBean.getMinVisibleVersion(), musicBean.getMaxVisibleVersion())) {
                    musicBeans.remove(i);
                }
            }
            if (musicBeans.size() == 0) {
                removes.add(musicGroup);
            }
        }
        musicGroups.removeAll(removes);
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
                } else if (mCacheFileNameSet.contains(StoreUtil.getTempCacheFileName(musicBean.getVersion(), StoreUtil.getNetFileName(musicBean.getUrl())))) {
                    musicBean.setState(MusicBean.STATE_DOWNLOAD_PAUSED);
                } else {
                    musicBean.setState(MusicBean.STATE_UNDOWNLOADED);
                }
            }
        }
    }

    /**
     * 提交线程加载音频分组列表数据
     */
    @Override
    public void loadMusicGroupListData() {
        // 有正处理的请求，直接返回，防止重复任务
        if (!mLoadingListTaskSet.isEmpty()) return;
        // new请求列表任务
        LoadMusicGroupListRunnable loadMusicGroupListRunnable = new LoadMusicGroupListRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, mLoadingListTaskSet, mMainHandler, mHandlerRunnables);
        // 标志设为正在请求列表信息
        mLoadingListTaskSet.add(loadMusicGroupListRunnable);
        // 提交线程池
        mThreadPoolExecutor.execute(loadMusicGroupListRunnable);
    }

    /**
     * 提交线程加载音频文件
     *
     * @param musicBean 音频信息
     */
    @Override
    public void loadMusicFileData(@NonNull MusicBean musicBean) {
        // 新的任务
        LoadMusicFileRunnable loadMusicFileRunnable = new LoadMusicFileRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, musicBean, mDownloadingTaskMap, mMainHandler, mHandlerRunnables);
        synchronized (mDownloadingTaskMap) {
            // 已存在任务则返回，防重
            if (mDownloadingTaskMap.containsKey(musicBean)) return;
            // 注册任务
            mDownloadingTaskMap.put(musicBean, loadMusicFileRunnable);
        }
        // 提交线程池
        mThreadPoolExecutor.execute(loadMusicFileRunnable);
    }

    /**
     * 提交线程删除音频文件
     *
     * @param musicBean 音频信息
     */
    @Override
    public void deleteMusicFile(@NonNull MusicBean musicBean) {
        DeleteMusicFileRunnable deleteMusicFileRunnable = new DeleteMusicFileRunnable(PRIORITY_DEFAULT, mIMusicManagerWeakReference, musicBean, mDeletingTaskMap, mMainHandler, mHandlerRunnables);
        synchronized (mDeletingTaskMap) {
            // 已存在任务则返回，防重
            if (mDeletingTaskMap.containsKey(musicBean)) return;
            // 注册任务
            mDeletingTaskMap.put(musicBean, deleteMusicFileRunnable);
        }
        // 提交线程池
        mThreadPoolExecutor.execute(deleteMusicFileRunnable);
    }

    @Override
    public void pauseLoading(@NonNull MusicBean musicBean) {
        // 通过musicBean拿到任务
        LoadMusicFileRunnable loadMusicFileRunnable = mDownloadingTaskMap.get(musicBean);
        // 拿不到就返回了
        if (loadMusicFileRunnable == null) return;
        // 暂停
        loadMusicFileRunnable.setPaused();
    }

    @Override
    public void stopLoading() {
        // 终结线程池
        if (mThreadPoolExecutor != null) {
            mThreadPoolExecutor.shutdownNow();
        }
        // 设置所有任务禁止回调主线程
        synchronized (mLoadingListTaskSet) {
            if (!mLoadingListTaskSet.isEmpty()) {
                for (LoadMusicGroupListRunnable loadMusicGroupListRunnable : mLoadingListTaskSet) {
                    loadMusicGroupListRunnable.setForbidCallback();
                }
            }
        }
        // 设置所有任务禁止回调主线程，并让该下载音乐任务暂停
        synchronized (mDownloadingTaskMap) {
            for (LoadMusicFileRunnable loadMusicFileRunnable : mDownloadingTaskMap.values()) {
                loadMusicFileRunnable.setPaused();
                loadMusicFileRunnable.setForbidCallback();
            }
        }
        // 设置所有任务禁止回调主线程
        synchronized (mDeletingTaskMap) {
            for (DeleteMusicFileRunnable deleteMusicFileRunnable : mDeletingTaskMap.values()) {
                deleteMusicFileRunnable.setForbidCallback();
            }
        }
        // 取消该handler提交的所有runnable
        synchronized (mMainHandler) {
            for (Runnable r : mHandlerRunnables) {
                mMainHandler.removeCallbacks(r);
            }
        }
    }

    /**
     * 加载音频信息分组列表的任务类
     */
    private static class LoadMusicGroupListRunnable extends BaseLoadRunnable {

        // 加载中的列表请求任务集，集合中只有一个任务
        private final HashSet<LoadMusicGroupListRunnable> mLoadingListTaskSet;

        LoadMusicGroupListRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, HashSet<LoadMusicGroupListRunnable> loadingListTaskMap, Handler handler, ArrayList<Runnable> runnables) {
            super(priority, iMusicManagerWeakReference, handler, runnables);
            this.mLoadingListTaskSet = loadingListTaskMap;
        }

        @Override
        void call() {
            // 获取网络列表
            ArrayList<MusicGroup> musicGroups = NetUtil.getMusicList();
            if (musicGroups == null) {
                // 网络列表获取不到则读缓存
                musicGroups = StoreUtil.readCacheMusicList();
                if (musicGroups == null) {
                    postAndMark(new Runnable() {
                        @Override
                        public void run() {
                            // 清除标记
                            synchronized (mHandlerRunnables) {
                                mHandlerRunnables.remove(this);
                            }
                            // 加载不到列表信息，回调加载失败
                            IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                            if (iMusicManager != null) {
                                iMusicManager.musicGroupListDataLoadedFailedCallback();
                            }
                        }
                    });
                    // 设置为没有列表请求
                    synchronized (mLoadingListTaskSet) {
                        mLoadingListTaskSet.remove(this);
                    }
                    return;
                }
            }
            // 过滤版本不可见的MusicBean
//            filterInvisibleMusicBean(musicGroups);
            // 检查并设置好MusicBean的状态（是否已下载或是下载中）
            checkMusicBeanState(musicGroups);
            final ArrayList<MusicGroup> finalMusicGroups = musicGroups;
            postAndMark(new Runnable() {
                @Override
                public void run() {
                    // 清除标记
                    synchronized (mHandlerRunnables) {
                        mHandlerRunnables.remove(this);
                    }
                    // 回调加载成功
                    IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                    if (iMusicManager != null) {
                        iMusicManager.musicGroupListDataLoadedCallback(finalMusicGroups);
                    }
                }
            });
            // 缓存列表信息
            StoreUtil.writeCacheMusicList(musicGroups);
            // 清理掉不存在于列表中的音乐文件
            StoreUtil.sortOutCache(musicGroups);
            // 设置为没有列表请求
            synchronized (mLoadingListTaskSet) {
                mLoadingListTaskSet.remove(this);
            }
        }
    }

    /**
     * 加载音频文件的任务类
     */
    private static class LoadMusicFileRunnable extends BaseLoadRunnable {

        // 注册池，记录进行中的下载任务
        private final HashMap<MusicBean, LoadMusicFileRunnable> mDownloadingTaskMap;
        // 音乐信息
        private MusicBean mMusicBean;
        // 原子可见标志位
        private volatile AtomicBoolean mPauseFlag;

        LoadMusicFileRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, MusicBean musicBean, HashMap<MusicBean, LoadMusicFileRunnable> downloadingTaskMap, Handler handler, ArrayList<Runnable> runnables) {
            super(priority, iMusicManagerWeakReference, handler, runnables);
            this.mMusicBean = musicBean;
            this.mDownloadingTaskMap = downloadingTaskMap;
            mPauseFlag = new AtomicBoolean(false);
            mForbidCallbackFlag = new AtomicBoolean(false);
        }

        @Override
        void call() {
            // 要下载的文件是否已经存在
            boolean exist = StoreUtil.findCacheFile(mMusicBean.getVersion(), StoreUtil.getNetFileName(mMusicBean.getUrl())) != null;
            if (exist) {
                postAndMark(new Runnable() {
                    @Override
                    public void run() {
                        // 清除标记
                        synchronized (mHandlerRunnables) {
                            mHandlerRunnables.remove(this);
                        }
                        // 回调加载成功
                        final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                        if (iMusicManager != null) {
                            iMusicManager.musicFileLoadedCallback(mMusicBean);
                        }
                    }
                });
                // 取消注册
                synchronized (mDownloadingTaskMap) {
                    mDownloadingTaskMap.remove(mMusicBean);
                }
                return;
            }
            // 加载网络文件数据
            boolean isSuccessful = NetUtil.downloadMusicFile(mMusicBean, mPauseFlag);
            // 如果数据为空，或者存储文件失败
            if (!isSuccessful) {
                final boolean isPaused = mPauseFlag.get();
                postAndMark(new Runnable() {
                    @Override
                    public void run() {
                        // 清除标记
                        synchronized (mHandlerRunnables) {
                            mHandlerRunnables.remove(this);
                        }
                        // 回调下载失败
                        final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                        if (iMusicManager == null) return;
                        if (isPaused) {
                            // 如果是被暂停了，回调暂停
                            iMusicManager.musicFileLoadingPausedCallback(mMusicBean);
                        } else {
                            // 不是暂停的说明下载出问题了，回调失败
                            iMusicManager.musicFileLoadedFailedCallback(mMusicBean);
                        }
                    }
                });
            } else {
                postAndMark(new Runnable() {
                    @Override
                    public void run() {
                        // 清除标记
                        synchronized (mHandlerRunnables) {
                            mHandlerRunnables.remove(this);
                        }
                        // 回调下载成功
                        final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                        if (iMusicManager == null) return;
                        iMusicManager.musicFileLoadedCallback(mMusicBean);
                    }
                });
            }
            // 取消注册
            synchronized (mDownloadingTaskMap) {
                mDownloadingTaskMap.remove(mMusicBean);
            }
        }

        // 设置线程暂停
        private void setPaused() {
            this.mPauseFlag.compareAndSet(false, true);
        }
    }

    /**
     * 删除音频文件的任务类
     */
    private static class DeleteMusicFileRunnable extends BaseLoadRunnable {

        // 音乐信息
        final MusicBean mMusicBean;
        // 正在删除中的音乐
        final HashMap<MusicBean, DeleteMusicFileRunnable> mDeletingMusicBeanMap;

        DeleteMusicFileRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, MusicBean musicBean, HashMap<MusicBean, DeleteMusicFileRunnable> deletingMusicBeanMap, Handler handler, ArrayList<Runnable> runnables) {
            super(priority, iMusicManagerWeakReference, handler, runnables);
            this.mMusicBean = musicBean;
            this.mDeletingMusicBeanMap = deletingMusicBeanMap;
        }

        @Override
        void call() {
            // 是否删除成功
            boolean deleted = StoreUtil.deleteCacheFile(mMusicBean.getVersion(), StoreUtil.getNetFileName(mMusicBean.getUrl()));
            if (deleted) {
                postAndMark(new Runnable() {
                    @Override
                    public void run() {
                        // 清除标记
                        synchronized (mHandlerRunnables) {
                            mHandlerRunnables.remove(this);
                        }
                        // 回调删除成功
                        final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                        if (iMusicManager == null) return;
                        iMusicManager.musicFileDeletedCallback(mMusicBean);
                    }
                });
            } else {
                postAndMark(new Runnable() {
                    @Override
                    public void run() {
                        // 清除标记
                        synchronized (mHandlerRunnables) {
                            mHandlerRunnables.remove(this);
                        }
                        // 回掉删除失败
                        final IMusicManager iMusicManager = iMusicManagerWeakReference.get();
                        if (iMusicManager == null) return;
                        iMusicManager.musicFileDeletedFailedCallback(mMusicBean);
                    }
                });
            }
            // 取消注册
            synchronized (mDeletingMusicBeanMap) {
                mDeletingMusicBeanMap.remove(mMusicBean);
            }
        }
    }

    /**
     * 线程池运行Runnable的基类
     */
    private static abstract class BaseLoadRunnable implements Runnable, Comparable<BaseLoadRunnable> {

        // 主线程Handler
        final Handler mMainHandler;
        // Handler提交的任务
        final ArrayList<Runnable> mHandlerRunnables;
        // 优先级
        private final int priority;
        // 视图层的弱引用
        WeakReference<IMusicManager> iMusicManagerWeakReference;
        // 停止回调标志位，true时线程不会post回调View更改UI
        volatile AtomicBoolean mForbidCallbackFlag;

        BaseLoadRunnable(int priority, WeakReference<IMusicManager> iMusicManagerWeakReference, Handler handler, ArrayList<Runnable> runnables) {
            this.priority = priority;
            this.iMusicManagerWeakReference = iMusicManagerWeakReference;
            this.mMainHandler = handler;
            this.mHandlerRunnables = runnables;
            this.mForbidCallbackFlag = new AtomicBoolean(false);
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

        // post到主线程并记录下这个runnable，在runnable执行时记录被移除，Activity在onDestroy的时候遍历这些记录的runnable并通过handler从MessageQueue中移除
        void postAndMark(Runnable runnable) {
            if (!mForbidCallbackFlag.get()) {
                synchronized (mMainHandler) {
                    synchronized (mHandlerRunnables) {
                        mMainHandler.post(runnable);
                        mHandlerRunnables.add(runnable);
                    }
                }
            }
        }

        // 设置任务禁止post主线程
        void setForbidCallback() {
            mForbidCallbackFlag.compareAndSet(false, true);
        }
    }

    ///这些是创造假数据的方法

//    /**
//     * 假数据
//     */
//    private static ArrayList<MusicGroup> createFalseData() {
//        ArrayList<MusicGroup> musicGroups = new ArrayList<>();
//        musicGroups.add(createFalseMusicGroup("iTunes", 6));
//        musicGroups.add(createFalseMusicGroup("欢乐", 9));
//        musicGroups.add(createFalseMusicGroup("节奏", 10));
//        musicGroups.add(createFalseMusicGroup("电影感", 16));
//        musicGroups.add(createFalseMusicGroup("轻音乐", 3));
//        return musicGroups;
//    }
//
//    /**
//     * 假音频分组
//     */
//    private static MusicGroup createFalseMusicGroup(String type, int num) {
//        MusicGroup musicGroup = new MusicGroup();
//        musicGroup.setSortName(type);
//        ArrayList<MusicBean> musicBeans = new ArrayList<>();
//        for (int i = 0; i < num; i++) {
//            MusicBean musicBean = new MusicBean();
//            musicBean.setTime("2000");
//            musicBean.setVersion("1.1");
//            musicBean.setAuthorName("高橋優");
//            musicBean.setId(String.valueOf(i));
//            musicBean.setName("ヤキモチ");
//            musicBean.setMinVisibleVersion("1.2");
//            musicBean.setMaxVisibleVersion("1.4");
//            musicBean.setUrl("http://dldir1.qq.com/weixin/android/weixin665android1280.apk");
//            musicBeans.add(musicBean);
//        }
//        musicGroup.setMusicBeans(musicBeans);
//        return musicGroup;
//    }
}
