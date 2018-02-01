package com.example.dell.smartrefreshlayoutdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.example.dell.smartrefreshlayoutdemo.widget.StatusBarUtil;
import com.example.dell.smartrefreshlayoutdemo.widget.TwoLevelHeader;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;

/**
 * Created by dell on 2018/2/1.
 */

public class SecondActivity extends AppCompatActivity {
    private RefreshLayout mRefreshLayout;
    private TwoLevelHeader header;
    private View floor;
    Toolbar toolbar;

    private ListView mListView;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initViews();

    }

    private void initViews() {

        mListView = findViewById(R.id.my_list);
        myAdapter = new MyAdapter(this);
        mListView.setAdapter(myAdapter);

        toolbar = findViewById(R.id.toolbar);
        floor = findViewById(R.id.secondfloor);
        header = findViewById(R.id.header);
        mRefreshLayout = findViewById(R.id.refreshLayout);

        mRefreshLayout.setOnMultiPurposeListener(new SimpleMultiPurposeListener(){
            @Override
            public void onHeaderPulling(RefreshHeader header, float percent, int offset, int headerHeight, int extendHeight) {
                toolbar.setAlpha(1 - Math.min(percent, 1));
                floor.setTranslationY(Math.min(offset - floor.getHeight() + toolbar.getHeight(), mRefreshLayout.getLayout().getHeight() - floor.getHeight()));
            }

            @Override
            public void onHeaderReleasing(RefreshHeader header, float percent, int offset, int footerHeight, int extendHeight) {
                toolbar.setAlpha(1 - Math.min(percent, 1));
                floor.setTranslationY(Math.min(offset - floor.getHeight() + toolbar.getHeight(), mRefreshLayout.getLayout().getHeight() - floor.getHeight()));
            }
        });

        header.setOnTwoLevelListener(new TwoLevelHeader.OnTwoLevelListener() {
            @SuppressLint("WrongViewCast")
            @Override
            public boolean onTwoLevel(RefreshLayout refreshLayout) {
                Toast.makeText(SecondActivity.this,"触发二楼事件",Toast.LENGTH_SHORT).show();
                findViewById(R.id.secondfloor_content).animate().alpha(1).setDuration(2000);
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        header.finishTwoLevel();
                        findViewById(R.id.secondfloor_content).animate().alpha(0).setDuration(1000);
                    }
                },5000);
                return true;//true 将会展开二楼状态 false 关闭刷新
            }
        });

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Toast.makeText(SecondActivity.this,"触发刷新事件",Toast.LENGTH_SHORT).show();
                mRefreshLayout.finishRefresh(2000);

            }
        });



        //状态栏透明和间距处理
        StatusBarUtil.immersive(this);
        StatusBarUtil.setMargin(this,  findViewById(R.id.classics));
        StatusBarUtil.setPaddingSmart(this, findViewById(R.id.toolbar));
        StatusBarUtil.setPaddingSmart(this, findViewById(R.id.contentPanel));

    }
}
