package com.example.administrator.musiceditingpanelproject;

import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.adapter.MusicPageViewPagerAdapter;
import com.example.administrator.musiceditingpanelproject.adapter.MusicSortRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.widget.PageIndicator;
import com.example.administrator.musiceditingpanelproject.util.CacheUtil;
import com.example.administrator.musiceditingpanelproject.util.VersionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class EditMusicActivity extends AppCompatActivity implements View.OnClickListener
        , MusicPageViewPagerAdapter.MusicItemClickListener
        , MusicSortRecyclerViewAdapter.ItemClickListener
        , ViewPager.OnPageChangeListener {

    // 未选中状态
    private static final int STATE_UNSELECTED = -1;
    // 播放器tag
    private static final String TAG_ASYNCPLAYER = "tag_AsyncPlayer";
    // 音频信息分组列表
    private ArrayList<MusicGroup> mMusicGroups;
    // 每个分类名对应一个ViewPagerAdapter
    private HashMap<String, MusicPageViewPagerAdapter> mMusicPageAdapterHashMap;
    // 分类内分页ViewPager
    private ViewPager mViewPagerMusicPage;
    // 底部音频分类名列表
    private RecyclerView mRecyclerViewMusicSort;
    // 分页指示器
    private PageIndicator mPageIndicator;
    // 编辑面板
    private RelativeLayout mRlPanel;
    // 关闭按钮
    private TextView mTvClose;
    // 删除按钮
    private ImageView mIvDelete;
    // 异步音乐播放器
    private AsyncPlayer mAsyncPlayer;
    // 下面分类名被选中的分类在RecyclerView中的position，如果是-1，就是没有分类被选中，保存该变量方便取消其被选中状态
    private int mSelectedMusicSortPositionInSortList = STATE_UNSELECTED;
    // 编辑板中最近一次被点击的Item的MusicBean
    private MusicBean mClickedMusicBeanInMusicGroup = null;
    /* 这四个变量用于确定目前被选中的Item的位置 如：欢乐分类下第2页第3项
       知道位置后方便取消其被选中状态 */
    // 编辑板中目前被选中的Item属于哪个分类
    private String mSelectedMusicBeanSort = null;
    // 编辑板中目前被选中的Item处于Viewpager中的第几页，-1为没有Item被选中
    private int mSelectedMusicBeanPageInMusicGroup = STATE_UNSELECTED;
    // 编辑板中目前被选中的Item处于一页中的第几项，-1为没有Item被选中
    private int mSelectedMusicBeanPositionInPage = STATE_UNSELECTED;
    // 编辑板中最近一次被选中的Item的MusicBean
    private MusicBean mSelectedMusicBeanInMusicGroup = null;

    /**
     * 顾名思义
     */
    private void initView() {
        mViewPagerMusicPage = findViewById(R.id.vp_items);
        mRecyclerViewMusicSort = findViewById(R.id.rv_sort);
        mPageIndicator = findViewById(R.id.page_indicator);
        mRlPanel = findViewById(R.id.rl_panel);
        mTvClose = findViewById(R.id.tv_close);
        mIvDelete = findViewById(R.id.iv_delete);
    }

    /**
     * 顾名思义
     */
    private void initData() {
        mMusicGroups = createFalseData();
        filterInvisibleMusicBean(mMusicGroups);
        checkMusicBeanState(mMusicGroups);
        mAsyncPlayer = new AsyncPlayer(TAG_ASYNCPLAYER);
        mMusicPageAdapterHashMap = new HashMap<>();
        mTvClose.setOnClickListener(this);
        mIvDelete.setOnClickListener(this);
        // RecyclerView设置LayoutManager
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewMusicSort.setLayoutManager(mLinearLayoutManager);
        // RecyclerView设置Adapter
        MusicSortRecyclerViewAdapter mMusicSortRecyclerViewAdapter = new MusicSortRecyclerViewAdapter(mMusicGroups);
        mMusicSortRecyclerViewAdapter.setItemClickListener(this);
        mRecyclerViewMusicSort.setAdapter(mMusicSortRecyclerViewAdapter);
        // ViewPager滑页监听
        mViewPagerMusicPage.addOnPageChangeListener(this);
    }

    /**
     * 过滤不可见音频信息
     * @param musicGroups 音频信息分组列表
     */
    private void filterInvisibleMusicBean(ArrayList<MusicGroup> musicGroups){
        for (MusicGroup musicGroup:musicGroups) {
            ArrayList<MusicBean> musicBeans = musicGroup.getMusicBeans();
            for (int i=musicBeans.size()-1;i>=0;i--){
                MusicBean musicBean = musicBeans.get(i);
                if (!VersionUtil.versionIsMatch(musicBean.getMinVisibleVersion(),musicBean.getMaxVisibleVersion())){
                    musicBeans.remove(i);
                }
            }
        }
    }

    /**
     * 检查音频信息分组列表中音频信息的状态，如音频信息存在缓存，则将state改为已下载
     * @param musicGroups 音频信息分组列表
     */
    private void checkMusicBeanState(ArrayList<MusicGroup> musicGroups){
        HashSet<String> mCacheFileNameSet = CacheUtil.getAllCacheFileName();
        if (mCacheFileNameSet == null) return;
        for (MusicGroup musicGroup:musicGroups) {
            for (MusicBean musicBean:musicGroup.getMusicBeans()) {
                if (mCacheFileNameSet.contains(CacheUtil.convertNameToFilename(musicBean.getVersion(),CacheUtil.getFileName(musicBean.getUrl())))){
                    musicBean.setState(MusicBean.STATE_DOWNLOADED);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_music_activity);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 编辑面板中按钮（关闭、删除）点击事件回调
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_close:
                ((MusicSortRecyclerViewAdapter.SortHolder) mRecyclerViewMusicSort.findViewHolderForAdapterPosition(mSelectedMusicSortPositionInSortList)).showUnClickedState();
                mSelectedMusicSortPositionInSortList = STATE_UNSELECTED;
                mRlPanel.setVisibility(View.GONE);
                break;
            case R.id.iv_delete:
                setSelectedItemUnselected(MusicBean.STATE_UNDOWNLOADED);
                break;
            default:
                break;
        }
    }

    /**
     * 这里是音频信息的Item点击监听器回调
     *
     * @param position 位置
     * @param holder   holder
     */
    @Override
    public void onMusicItemClicked(final int position, final MusicListRecyclerViewAdapter.ItemHolder holder, final MusicBean musicBean, final int pageIndex, final String sort) {
        mClickedMusicBeanInMusicGroup = musicBean;
        switch (musicBean.getState()){
            case MusicBean.STATE_UNDOWNLOADED:
                holder.showDownloadingState();
                musicBean.setState(MusicBean.STATE_DOWNLOADING);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.showDownloadedState();
                                    musicBean.setState(MusicBean.STATE_DOWNLOADED);
                                    if (musicBean == mClickedMusicBeanInMusicGroup) {
                                        holder.showEditState();
                                        musicBean.setState(MusicBean.STATE_EDIT);
                                        setSelectedItemUnselected(MusicBean.STATE_DOWNLOADED);
                                        mSelectedMusicBeanSort = sort;
                                        mSelectedMusicBeanPositionInPage = position;
                                        mSelectedMusicBeanPageInMusicGroup = pageIndex;
                                        mSelectedMusicBeanInMusicGroup = musicBean;
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case MusicBean.STATE_DOWNLOADING:
                break;
            case MusicBean.STATE_DOWNLOADED:
                setSelectedItemUnselected(MusicBean.STATE_DOWNLOADED);
                holder.showEditState();
                musicBean.setState(MusicBean.STATE_EDIT);
                mAsyncPlayer.play(MusicEditingPanelApplication.getApplication(), Uri.parse(CacheUtil.getCacheFileAbsolutePath(musicBean.getVersion(),CacheUtil.getFileName(musicBean.getUrl()))), true, AudioManager.STREAM_MUSIC);
                mSelectedMusicBeanSort = sort;
                mSelectedMusicBeanPositionInPage = position;
                mSelectedMusicBeanPageInMusicGroup = pageIndex;
                mSelectedMusicBeanInMusicGroup = musicBean;
                break;
            case MusicBean.STATE_EDIT:
                Toast.makeText(this,"编辑",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 将被选中的Item设为未被选中
     */
    private void setSelectedItemUnselected(int state) {
        if (mSelectedMusicBeanPageInMusicGroup != STATE_UNSELECTED && mSelectedMusicBeanPositionInPage != STATE_UNSELECTED && mSelectedMusicBeanSort != null && mSelectedMusicBeanInMusicGroup!=null) {
            MusicListRecyclerViewAdapter.ItemHolder holder = (MusicListRecyclerViewAdapter.ItemHolder) mMusicPageAdapterHashMap.get(mSelectedMusicBeanSort).getRecyclerViews().get(mSelectedMusicBeanPageInMusicGroup).findViewHolderForAdapterPosition(mSelectedMusicBeanPositionInPage);
            switch (state){
                case MusicBean.STATE_UNDOWNLOADED:
                    holder.showUndownloadedState();
                    break;
                case MusicBean.STATE_DOWNLOADING:
                    holder.showDownloadingState();
                    break;
                case MusicBean.STATE_DOWNLOADED:
                    holder.showDownloadedState();
                    break;
                case MusicBean.STATE_EDIT:
                    holder.showEditState();
                    break;
                default:
                    break;
            }
            mSelectedMusicBeanInMusicGroup.setState(state);
            mSelectedMusicBeanPageInMusicGroup = STATE_UNSELECTED;
            mSelectedMusicBeanPositionInPage = STATE_UNSELECTED;
            mSelectedMusicBeanSort = null;
            mSelectedMusicBeanInMusicGroup = null;
        }

    }

    /**
     * 这里是底部分类名列表的Item点击监听器回调
     *
     * @param position 位置
     * @param holder   holder
     */
    @Override
    public void OnItemClick(int position, MusicSortRecyclerViewAdapter.SortHolder holder) {
        // 如果mClickedMusicSortPositionInSortList不等于STATE_UNSELECTED说明有其他分类被选中
        if (mSelectedMusicSortPositionInSortList != STATE_UNSELECTED) {
            // 将其置为非选中状态
            ((MusicSortRecyclerViewAdapter.SortHolder) mRecyclerViewMusicSort.findViewHolderForAdapterPosition(mSelectedMusicSortPositionInSortList)).showUnClickedState();
        }
        // 将目前点击的position设置给mClickedMusicSortPositionInSortList
        mSelectedMusicSortPositionInSortList = position;
        // 置为选中状态
        holder.showClickedState();
        // 显示编辑面板
        mRlPanel.setVisibility(View.VISIBLE);
        // 分类对应的musicGroup
        MusicGroup musicGroup = mMusicGroups.get(position);
        // HashSet中找找有没有对应的ViewPagerAdapter
        MusicPageViewPagerAdapter musicPageViewPagerAdapter = mMusicPageAdapterHashMap.get(musicGroup.getSortName());
        // 如果没有就新建一个Adapter丢进HashSet中，分类名为key
        if (musicPageViewPagerAdapter == null) {
            musicPageViewPagerAdapter = new MusicPageViewPagerAdapter(mViewPagerMusicPage, musicGroup);
            musicPageViewPagerAdapter.setMusicItemClickListener(EditMusicActivity.this);
            mMusicPageAdapterHashMap.put(musicGroup.getSortName(), musicPageViewPagerAdapter);
        }
        // 设置Adapter
        mViewPagerMusicPage.setAdapter(musicPageViewPagerAdapter);
        // 更新页码指示器
        mPageIndicator.setTotalPages(musicPageViewPagerAdapter.getPageCount());
    }

    /**
     * ViewPager的滑动回调
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * ViewPager的滑页回调
     */
    @Override
    public void onPageSelected(int position) {
        mPageIndicator.setCurrentPageIndex(position + 1);
    }

    /**
     * ViewPager的滑动状态回调
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 假数据
     */
    private ArrayList<MusicGroup> createFalseData() {
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
    private MusicGroup createFalseMusicGroup(String type, int num) {
        MusicGroup musicGroup = new MusicGroup();
        musicGroup.setSortName(type);
        ArrayList<MusicBean> musicBeans = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            MusicBean musicBean = new MusicBean();
            musicBean.setTime("2000");
            musicBean.setVersion("1.1");
            musicBean.setAuthorName("James Blunt");
            musicBean.setId(String.valueOf(i));
            musicBean.setName("You're Beautiful");
            musicBean.setMinVisibleVersion("1.2");
            musicBean.setMaxVisibleVersion("1.4");
            musicBean.setUrl("https://material.storage.mixvvideo.com/1521601330799.gif");
            musicBeans.add(musicBean);
        }
        musicGroup.setMusicBeans(musicBeans);
        return musicGroup;
    }

}
