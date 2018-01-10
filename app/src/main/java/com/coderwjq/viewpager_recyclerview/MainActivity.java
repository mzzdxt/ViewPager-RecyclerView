package com.coderwjq.viewpager_recyclerview;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomePageManager.OnModeChangeListener {
    private static final String TAG = "MainActivity";

    private HomeRecyclerView mRvHomePage;
    private HomePageAdapter mHomePageAdapter;
    private TabLayout mTlNewsTitle;
    private LinearLayout mLlMenuBar;
    private Button mBtnBackHome;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRvHomePage = findViewById(R.id.rv_home_page);
        mTlNewsTitle = findViewById(R.id.tl_news_title);
        mLlMenuBar = findViewById(R.id.ll_menu_bar);
        mBtnBackHome = findViewById(R.id.btn_back_home);

        mHomePageAdapter = new HomePageAdapter();
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvHomePage.setLayoutManager(mLayoutManager);
        mRvHomePage.setAdapter(mHomePageAdapter);
        mRvHomePage.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mBtnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomePageManager.getInstance().setNormalMode();

                if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                    Log.i(TAG, "smoothScrollToPosition: 0");
                    mRvHomePage.smoothScrollToPosition(0);
                }
            }
        });

        HomePageManager.getInstance().attatchModeChangeListener(this);
    }

    @Override
    public void refreshMode(int currentMode) {
        if (currentMode == HomePageManager.HOME_PAGE_MODE_NORMAL) {
            mTlNewsTitle.setVisibility(View.INVISIBLE);
        } else if (currentMode == HomePageManager.HOME_PAGE_MODE_NEWS) {
            mTlNewsTitle.setVisibility(View.VISIBLE);
        } else {
            // 其他状态
        }
    }

    class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HOME_PAGE_ITEM_COUNT = 30;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                // 普通item
                return new NormalViewHolder(getLayoutInflater().inflate(R.layout.item_home_page_normal, null));
            } else {
                return new NewsViewHolder(getLayoutInflater().inflate(R.layout.item_home_page_news, null));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof NormalViewHolder) {
                NormalViewHolder holder = (NormalViewHolder) viewHolder;
                holder.mTvContent.setText("item: " + position);
            } else if (viewHolder instanceof NewsViewHolder) {
                NewsViewHolder holder = (NewsViewHolder) viewHolder;
                NewsAdapter newsAdapter = new NewsAdapter(getSupportFragmentManager());
                holder.mVpContainer.setAdapter(newsAdapter);

                mTlNewsTitle.setupWithViewPager(holder.mVpContainer);
                mTlNewsTitle.getTabAt(0).setText("文本新闻");
                mTlNewsTitle.getTabAt(1).setText("视频新闻");

                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                Log.e(TAG, "屏幕高度: " + dm.heightPixels);
                Log.e(TAG, "菜单栏高度: " + mLlMenuBar.getHeight());
                Log.e(TAG, "TabLayout高度: " + mTlNewsTitle.getHeight());

                //应用区域
                Rect outRect1 = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
                // 状态栏高度 = 屏幕高度 - 应用区域高度
                int statusBar = dm.heightPixels - outRect1.height();
                Log.e(TAG, "状态栏高度: " + statusBar);

                // 设置ViewPager高度
                ViewGroup.LayoutParams layoutParams = holder.mVpContainer.getLayoutParams();
                layoutParams.height = dm.heightPixels - mLlMenuBar.getHeight() - mTlNewsTitle.getHeight() - statusBar;
            }
        }

        @Override
        public int getItemCount() {
            return HOME_PAGE_ITEM_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < HOME_PAGE_ITEM_COUNT - 1) {
                return 0;
            } else {
                return 1;
            }
        }

        class NormalViewHolder extends RecyclerView.ViewHolder {

            private final TextView mTvContent;

            public NormalViewHolder(View itemView) {
                super(itemView);
                mTvContent = itemView.findViewById(R.id.tv_content);
            }
        }

        class NewsViewHolder extends RecyclerView.ViewHolder {

            private final NewsViewPager mVpContainer;

            public NewsViewHolder(View itemView) {
                super(itemView);
                mVpContainer = itemView.findViewById(R.id.vp_container);
            }
        }

        class NewsAdapter extends FragmentPagerAdapter {
            private List<Fragment> mFragments = new ArrayList<>();

            public NewsAdapter(FragmentManager fm) {
                super(fm);
                mFragments.add(new TextNewsFragment());
                mFragments.add(new VideoNewsFragment());
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        }
    }
}
