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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
    // 唯一实例
    private volatile static EditMusicPanelLoader editMusicPanelLoader = null;
    // 线程池
    private final ThreadPoolExecutor mThreadPoolExecutor;
    // 是否有已提交的列表任务
    private final HashSet<LoadMusicGroupListRunnable> mLoadingListTaskSet;
    // 已提交的文件任务
    private final ConcurrentHashMap<MusicBean, LoadMusicFileRunnable> mDownloadingTaskMap;
    // 已提交的删除任务
    private final ConcurrentHashMap<MusicBean, DeleteMusicFileRunnable> mDeletingTaskMap;
    // 主线程Handler，用来回调
    private final Handler mMainHandler;
    // 观察者们
    private final ArrayList<IMusicManager> mMusicManagers;

    private EditMusicPanelLoader() {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int NUMBER_OF_MAX = 2 * NUMBER_OF_CORES;
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_MAX, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        mLoadingListTaskSet = new HashSet<>(1);
        mDownloadingTaskMap = new ConcurrentHashMap<>();
        mDeletingTaskMap = new ConcurrentHashMap<>();
        mMainHandler = new Handler(Looper.getMainLooper());
        mMusicManagers = new ArrayList<>();
    }

    public static EditMusicPanelLoader getInstance() {
        if (editMusicPanelLoader == null) {
            synchronized (EditMusicPanelLoader.class) {
                if (editMusicPanelLoader == null) {
                    editMusicPanelLoader = new EditMusicPanelLoader();
                }
            }
        }
        return editMusicPanelLoader;
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
            // 如果过滤后的分组没有musicBean了，则移除该分组
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
                // 如果存在名为 版本+“@#”+网络文件名 的文件，则说明文件该网络文件下载完毕，以此类推
                if (mCacheFileNameSet.contains(StoreUtil.getCacheFileName(musicBean.getVersion(), StoreUtil.getNetFileName(musicBean.getUrl())))) {
                    musicBean.setState(MusicBean.STATE_DOWNLOADED);
                } else if (mCacheFileNameSet.contains(StoreUtil.getDownloadingCacheFileName(musicBean.getVersion(), StoreUtil.getNetFileName(musicBean.getUrl())))) {
                    musicBean.setState(MusicBean.STATE_DOWNLOADING);
                } else if (mCacheFileNameSet.contains(StoreUtil.getPausedCacheFileName(musicBean.getVersion(), StoreUtil.getNetFileName(musicBean.getUrl())))) {
                    musicBean.setState(MusicBean.STATE_DOWNLOAD_PAUSED);
                } else {
                    musicBean.setState(MusicBean.STATE_UNDOWNLOADED);
                }
            }
        }
    }

    @Override
    public void registerMusicManager(IMusicManager iMusicManager) {
        mMusicManagers.add(iMusicManager);
    }

    @Override
    public void unregisterMusicManager(IMusicManager iMusicManager) {
        mMusicManagers.remove(iMusicManager);
    }

    /**
     * 提交线程加载音频分组列表数据
     */
    @Override
    public void loadMusicGroupListData() {
        // new请求列表任务
        LoadMusicGroupListRunnable loadMusicGroupListRunnable = new LoadMusicGroupListRunnable(PRIORITY_DEFAULT, mMusicManagers, mLoadingListTaskSet, mMainHandler);
        // 有正处理的请求，直接返回，防止重复任务
        synchronized (mLoadingListTaskSet) {
            if (!mLoadingListTaskSet.isEmpty()) return;
            // 标志设为正在请求列表信息
            mLoadingListTaskSet.add(loadMusicGroupListRunnable);
        }
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
        LoadMusicFileRunnable loadMusicFileRunnable = new LoadMusicFileRunnable(PRIORITY_DEFAULT, mMusicManagers, musicBean, mDownloadingTaskMap, mMainHandler);
        // 已存在任务则返回，防重
        if (mDownloadingTaskMap.containsKey(musicBean)) return;
        // 注册任务
        mDownloadingTaskMap.put(musicBean, loadMusicFileRunnable);
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
        DeleteMusicFileRunnable deleteMusicFileRunnable = new DeleteMusicFileRunnable(PRIORITY_DEFAULT, mMusicManagers, musicBean, mDeletingTaskMap, mMainHandler);
        // 已存在任务则返回，防重
        if (mDeletingTaskMap.containsKey(musicBean)) return;
        // 注册任务
        mDeletingTaskMap.put(musicBean, deleteMusicFileRunnable);
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

    /**
     * 加载音频信息分组列表的任务类
     */
    private static class LoadMusicGroupListRunnable extends BaseLoadRunnable {

        // 加载中的列表请求任务集，集合中只有一个任务
        private final HashSet<LoadMusicGroupListRunnable> mLoadingListTaskSet;

        LoadMusicGroupListRunnable(int priority, ArrayList<IMusicManager> musicManagers, HashSet<LoadMusicGroupListRunnable> loadingListTaskMap, Handler handler) {
            super(priority, musicManagers, handler);
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
                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            // 加载不到列表信息，回调加载失败
                            for (IMusicManager iMusicManager : mMusicManagers) {
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
            filterInvisibleMusicBean(musicGroups);
            // 检查并设置好MusicBean的状态（是否已下载或是下载中）
            checkMusicBeanState(musicGroups);
            final ArrayList<MusicGroup> finalMusicGroups = musicGroups;
            postMainThread(new Runnable() {
                @Override
                public void run() {
                    // 回调加载成功
                    for (IMusicManager iMusicManager : mMusicManagers) {
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
        private final ConcurrentHashMap<MusicBean, LoadMusicFileRunnable> mDownloadingTaskMap;
        // 音乐信息
        private MusicBean mMusicBean;
        // 原子可见标志位
        private volatile AtomicBoolean mPauseFlag;

        LoadMusicFileRunnable(int priority, ArrayList<IMusicManager> musicManagers, MusicBean musicBean, ConcurrentHashMap<MusicBean, LoadMusicFileRunnable> downloadingTaskMap, Handler handler) {
            super(priority, musicManagers, handler);
            this.mMusicBean = musicBean;
            this.mDownloadingTaskMap = downloadingTaskMap;
            mPauseFlag = new AtomicBoolean(false);
        }

        @Override
        void call() {
            // 要下载的文件是否已经存在
            boolean exist = StoreUtil.findCacheFile(mMusicBean.getVersion(), StoreUtil.getNetFileName(mMusicBean.getUrl())) != null;
            if (exist) {
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // 回调加载成功
                        for (IMusicManager iMusicManager : mMusicManagers) {
                            iMusicManager.musicFileLoadedCallback(mMusicBean);
                        }
                    }
                });
                // 取消注册
                mDownloadingTaskMap.remove(mMusicBean);
                return;
            }

            // 加载网络文件数据
            boolean isSuccessful = NetUtil.downloadMusicFile(mMusicBean, mPauseFlag);
            // 如果数据为空，或者存储文件失败
            if (!isSuccessful) {
                final boolean isPaused = mPauseFlag.get();
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // 回调下载失败
                        for (IMusicManager iMusicManager : mMusicManagers) {
                            if (isPaused) {
                                // 如果是被暂停了，回调暂停
                                iMusicManager.musicFileLoadingPausedCallback(mMusicBean);
                            } else {
                                // 不是暂停的说明下载出问题了，回调失败
                                iMusicManager.musicFileLoadedFailedCallback(mMusicBean);
                            }
                        }
                    }
                });
            } else {
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // 回调下载成功
                        for (IMusicManager iMusicManager : mMusicManagers) {
                            iMusicManager.musicFileLoadedCallback(mMusicBean);
                        }
                    }
                });
            }
            // 取消注册
            mDownloadingTaskMap.remove(mMusicBean);
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
        final ConcurrentHashMap<MusicBean, DeleteMusicFileRunnable> mDeletingMusicBeanMap;

        DeleteMusicFileRunnable(int priority, ArrayList<IMusicManager> musicManagers, MusicBean musicBean, ConcurrentHashMap<MusicBean, DeleteMusicFileRunnable> deletingMusicBeanMap, Handler handler) {
            super(priority, musicManagers, handler);
            this.mMusicBean = musicBean;
            this.mDeletingMusicBeanMap = deletingMusicBeanMap;
        }

        @Override
        void call() {
            // 是否删除成功
            boolean deleted = StoreUtil.deleteCacheFile(mMusicBean.getVersion(), StoreUtil.getNetFileName(mMusicBean.getUrl()));
            if (deleted) {
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // 回调删除成功
                        for (IMusicManager iMusicManager : mMusicManagers) {
                            iMusicManager.musicFileDeletedCallback(mMusicBean);
                        }
                    }
                });
            } else {
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // 回掉删除失败
                        for (IMusicManager iMusicManager : mMusicManagers) {
                            iMusicManager.musicFileDeletedFailedCallback(mMusicBean);
                        }
                    }
                });
            }
            // 取消注册
            mDeletingMusicBeanMap.remove(mMusicBean);
        }
    }

    /**
     * 线程池运行Runnable的基类
     */
    private static abstract class BaseLoadRunnable implements Runnable, Comparable<BaseLoadRunnable> {

        // 主线程Handler
        final Handler mMainHandler;
        // 观察者们
        final ArrayList<IMusicManager> mMusicManagers;
        // 优先级
        private final int priority;

        BaseLoadRunnable(int priority, ArrayList<IMusicManager> musicManagers, Handler handler) {
            this.priority = priority;
            this.mMusicManagers = musicManagers;
            this.mMainHandler = handler;
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
        void postMainThread(Runnable runnable) {
            synchronized (mMainHandler) {
                mMainHandler.post(runnable);
            }
        }
    }

    ///这些是创造假数据的方法

//    /**
//     * 假数据
//     */
//    private static ArrayList<MusicGroup> createFalseData() {
//        ArrayList<MusicGroup> musicGroups = new ArrayList<>();
//        musicGroups.add(createFalseMusicGroup("iTunes", 1));
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
