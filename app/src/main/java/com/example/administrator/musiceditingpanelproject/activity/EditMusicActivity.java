package com.example.administrator.musiceditingpanelproject.activity;

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

import com.example.administrator.musiceditingpanelproject.R;
import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.adapter.MusicPageViewPagerAdapter;
import com.example.administrator.musiceditingpanelproject.adapter.MusicSortRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;
import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;
import com.example.administrator.musiceditingpanelproject.presenter.EditMusicPanelManager;
import com.example.administrator.musiceditingpanelproject.presenter.IMusicManager;
import com.example.administrator.musiceditingpanelproject.view.IEditMusicPanel;
import com.example.administrator.musiceditingpanelproject.widget.PageIndicator;

import java.util.ArrayList;
import java.util.HashMap;

public class EditMusicActivity extends AppCompatActivity implements View.OnClickListener
        , MusicPageViewPagerAdapter.MusicItemClickListener
        , MusicSortRecyclerViewAdapter.ItemClickListener
        , ViewPager.OnPageChangeListener
        , IEditMusicPanel{

    // 未选中状态
    private static final int STATE_UNSELECTED = -1;
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
    //
    private MusicSortRecyclerViewAdapter mMusicSortRecyclerViewAdapter;
    //
    private IMusicManager iMusicManager;

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
        iMusicManager = new EditMusicPanelManager(this);
        mMusicGroups = new ArrayList<>();
        mMusicPageAdapterHashMap = new HashMap<>();
        mTvClose.setOnClickListener(this);
        mIvDelete.setOnClickListener(this);
        // RecyclerView设置LayoutManager
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewMusicSort.setLayoutManager(mLinearLayoutManager);
        // RecyclerView设置Adapter
        mMusicSortRecyclerViewAdapter = new MusicSortRecyclerViewAdapter(mMusicGroups);
        mMusicSortRecyclerViewAdapter.setItemClickListener(this);
        mRecyclerViewMusicSort.setAdapter(mMusicSortRecyclerViewAdapter);
        // ViewPager滑页监听
        mViewPagerMusicPage.addOnPageChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_music_activity);
        initView();
        initData();
        iMusicManager.refreshMusicEditPanel();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        iMusicManager.stopMusic();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        iMusicManager.replayMusic();
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
                iMusicManager.deleteMusic(mSelectedMusicBeanInMusicGroup,(MusicListRecyclerViewAdapter.ItemHolder) mMusicPageAdapterHashMap.get(mSelectedMusicBeanSort).getRecyclerViews().get(mSelectedMusicBeanPageInMusicGroup).findViewHolderForAdapterPosition(mSelectedMusicBeanPositionInPage));

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
                iMusicManager.downloadMusic(musicBean,holder,sort,pageIndex,position);
                break;
            case MusicBean.STATE_DOWNLOADING:
                break;
            case MusicBean.STATE_DOWNLOADED:
                setSelectedItemUnselected(MusicBean.STATE_DOWNLOADED);
                holder.showEditState();
                musicBean.setState(MusicBean.STATE_EDIT);
                mSelectedMusicBeanSort = sort;
                mSelectedMusicBeanPositionInPage = position;
                mSelectedMusicBeanPageInMusicGroup = pageIndex;
                mSelectedMusicBeanInMusicGroup = musicBean;
                iMusicManager.playMusic(musicBean);
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

    @Override
    public void musicBeanStateChangedCallback(MusicListRecyclerViewAdapter.ItemHolder holder, MusicBean musicBean) {
        switch (musicBean.getState()){
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
    }

    @Override
    public void musicGroupListLoadedCallback(ArrayList<MusicGroup> musicGroups) {
        mMusicGroups.clear();
        mMusicGroups.addAll(musicGroups);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMusicSortRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void musicGroupListDataLoadedFailedCallback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicEditingPanelApplication.getApplication(),"拉取列表失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void musicFileDataLoadedCallback(MusicBean musicBean, MusicListRecyclerViewAdapter.ItemHolder itemHolder,String sort,int page,int position) {
        if (musicBean == mClickedMusicBeanInMusicGroup) {
            setSelectedItemUnselected(MusicBean.STATE_DOWNLOADED);
            itemHolder.showEditState();
            musicBean.setState(MusicBean.STATE_EDIT);
            mSelectedMusicBeanSort = sort;
            mSelectedMusicBeanPositionInPage = position;
            mSelectedMusicBeanPageInMusicGroup = page;
            mSelectedMusicBeanInMusicGroup = musicBean;
            iMusicManager.playMusic(musicBean);
        }
    }

    @Override
    public void musicFileDataLoadedFailedCallback(MusicBean musicBean) {
        Toast.makeText(this,"下载音频失败",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void musicFileDataDeletedCallback(MusicBean musicBean) {
        Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
        mSelectedMusicBeanPageInMusicGroup = STATE_UNSELECTED;
        mSelectedMusicBeanPositionInPage = STATE_UNSELECTED;
        mSelectedMusicBeanSort = null;
        mSelectedMusicBeanInMusicGroup = null;
    }

    @Override
    public void musicFileDataDeletedFailedCallback(MusicBean musicBean) {
        Toast.makeText(this,"删除失败",Toast.LENGTH_SHORT).show();
    }



}
