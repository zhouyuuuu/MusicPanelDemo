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
import com.example.administrator.musiceditingpanelproject.adapter.MusicListRecyclerViewAdapter.ItemHolder;
import com.example.administrator.musiceditingpanelproject.adapter.MusicPageViewPagerAdapter;
import com.example.administrator.musiceditingpanelproject.adapter.MusicSortRecyclerViewAdapter;
import com.example.administrator.musiceditingpanelproject.adapter.MusicSortRecyclerViewAdapter.SortHolder;
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
        , IEditMusicPanel {

    // 每帧绘画时间
    private static final int TIME_PER_FRAME = 100;
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
    // 编辑板中最近一次被选中的Item的MusicBean
    private MusicBean mSelectedMusicBean = null;
    // 分组列表适配器
    private MusicSortRecyclerViewAdapter mMusicSortRecyclerViewAdapter;
    // 音频管理者
    private IMusicManager iMusicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_music_activity);
        initView();
        initData();
        iMusicManager.refreshMusicEditPanel();
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
     * 初始化视图
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
     * 初始化数据
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

    /**
     * 编辑面板中按钮（关闭、删除）点击事件回调
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_close:
                ((SortHolder) mRecyclerViewMusicSort.findViewHolderForAdapterPosition(mSelectedMusicSortPositionInSortList)).showUnClickedState();
                mSelectedMusicSortPositionInSortList = STATE_UNSELECTED;
                mRlPanel.setVisibility(View.GONE);
                break;
            case R.id.iv_delete:
                iMusicManager.deleteMusic(mSelectedMusicBean);
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
    public void onMusicItemClicked(final int position, final ItemHolder holder, final MusicBean musicBean, final int pageIndex, final String sort) {
        mClickedMusicBeanInMusicGroup = musicBean;
        switch (musicBean.getState()) {
            case MusicBean.STATE_UNDOWNLOADED:
                iMusicManager.downloadMusic(musicBean);
                break;
            case MusicBean.STATE_DOWNLOADING:
                break;
            case MusicBean.STATE_DOWNLOADED:
                setSelectedItemUnselected(MusicBean.STATE_DOWNLOADED);
                holder.showEditState();
                musicBean.setState(MusicBean.STATE_EDIT);
                mSelectedMusicBean = musicBean;
                iMusicManager.playMusic(musicBean);
                break;
            case MusicBean.STATE_EDIT:
                Toast.makeText(this, "编辑", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 将被选中的Item设为未被选中
     */
    private void setSelectedItemUnselected(int state) {
        if (mSelectedMusicBean != null) {
            mSelectedMusicBean.setState(state);
            musicBeanStateChangedCallback(mSelectedMusicBean);
            mSelectedMusicBean = null;
        }

    }

    /**
     * 这里是底部分类名列表的Item点击监听器回调
     *
     * @param position 位置
     * @param holder   holder
     */
    @Override
    public void OnItemClick(int position, SortHolder holder) {
        // 如果mClickedMusicSortPositionInSortList不等于STATE_UNSELECTED说明有其他分类被选中
        if (mSelectedMusicSortPositionInSortList != STATE_UNSELECTED) {
            // 将其置为非选中状态
            ((SortHolder) mRecyclerViewMusicSort.findViewHolderForAdapterPosition(mSelectedMusicSortPositionInSortList)).showUnClickedState();
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
     * ViewPager的滑页回调
     */
    @Override
    public void onPageSelected(int position) {
        mPageIndicator.setCurrentPageIndex(position + 1);
    }

    /**
     * musicBean状态改变回调，面板Item显示对应的状态
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicBeanStateChangedCallback(final MusicBean musicBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int page;
                int position;
                String sort;
                for (MusicGroup musicGroup:mMusicGroups) {
                    ArrayList<MusicBean> musicBeans = musicGroup.getMusicBeans();
                    for (int i=0;i<musicBeans.size();i++){
                        if (musicBeans.get(i) == musicBean){
                            sort = musicGroup.getSortName();
                            page = i/ MusicPageViewPagerAdapter.ITEM_COUNT_PER_PAGE;
                            position = i%MusicPageViewPagerAdapter.ITEM_COUNT_PER_PAGE;
                            if (musicBean.getState() == MusicBean.STATE_EDIT){
                                mSelectedMusicBean = musicBean;
                                iMusicManager.playMusic(musicBean);
                            }
                            ItemHolder holder = (ItemHolder) mMusicPageAdapterHashMap
                                    .get(sort)
                                    .getRecyclerViews()
                                    .get(page)
                                    .findViewHolderForAdapterPosition(position);
                            if (holder != null) {
                                freshHolder(musicBean,holder);
                            }
                            return;
                        }
                    }
                }
            }
        });
    }

    /**
     * 加载列表成功，刷新分类列表和中间的ViewPager，选中分类列表的第一项，必须延迟100毫秒等带下一帧绘制完成才能获取到holder
     * 绘制1帧大概16毫秒，用100毫秒怕机子卡了，且100毫秒延迟并不明显
     *
     * @param musicGroups 音频信息分组列表
     */
    @Override
    public void musicGroupListLoadedCallback(final ArrayList<MusicGroup> musicGroups) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMusicGroups.clear();
                mMusicGroups.addAll(musicGroups);
                mMusicSortRecyclerViewAdapter.notifyDataSetChanged();
                // 将目前点击的position设置给mClickedMusicSortPositionInSortList
                mSelectedMusicSortPositionInSortList = 0;
                // 显示编辑面板
                mRlPanel.setVisibility(View.VISIBLE);
                // 分类对应的musicGroup
                MusicGroup musicGroup = mMusicGroups.get(0);
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
                mIvDelete.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SortHolder holder = (SortHolder) mRecyclerViewMusicSort.findViewHolderForAdapterPosition(mSelectedMusicSortPositionInSortList);
                        // 置为选中状态
                        holder.showClickedState();
                    }
                }, TIME_PER_FRAME);
            }
        });
    }

    /**
     * 加载列表失败弹出消息
     */
    @Override
    public void musicGroupListDataLoadedFailedCallback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicEditingPanelApplication.getApplication(), "拉取列表失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加载文件成功回调，如果musicBean是最近点击的musicBean则将其选中，记录选中的分页分类位置，清楚上一个选中项的选中状态，然后播放音乐
     *
     * @param musicBean  音频信息
     */
    @Override
    public void musicFileDataLoadedCallback(final MusicBean musicBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicBean == mClickedMusicBeanInMusicGroup) {
                    setSelectedItemUnselected(MusicBean.STATE_DOWNLOADED);
                    musicBean.setState(MusicBean.STATE_EDIT);
                    musicBeanStateChangedCallback(musicBean);
                }
            }
        });
    }

    /**
     * 下载失败回调，弹出消息
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicFileDataLoadedFailedCallback(MusicBean musicBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicEditingPanelApplication.getApplication(), "下载音频失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 删除成功回调，弹出消息
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicFileDataDeletedCallback(MusicBean musicBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicEditingPanelApplication.getApplication(), "删除成功", Toast.LENGTH_SHORT).show();
                mSelectedMusicBean = null;
            }
        });
    }

    /**
     * 删除失败回掉，弹出消息
     *
     * @param musicBean 音频信息
     */
    @Override
    public void musicFileDataDeletedFailedCallback(MusicBean musicBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicEditingPanelApplication.getApplication(), "删除失败", Toast.LENGTH_SHORT).show();
                mSelectedMusicBean = null;
            }
        });
    }

    /**
     * 确定MusicBean的位置，然后拿到对应的holder进行更新,如果为空，则不需要更新，因为再次显示时RecyclerView会重新刷新View
     * @param musicBean 音频信息
     */
    private void freshHolder(MusicBean musicBean,ItemHolder holder){
        switch (musicBean.getState()) {
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

    /**
     * ViewPager的滑动状态回调
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * ViewPager的滑动回调
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
}
