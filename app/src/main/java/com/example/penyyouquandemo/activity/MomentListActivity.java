package com.example.penyyouquandemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.penyyouquandemo.R;
import com.example.penyyouquandemo.adapter.CircleAdapter;
import com.example.penyyouquandemo.bean.CircleBean;
import com.example.penyyouquandemo.model.Moment;
import com.example.penyyouquandemo.util.AssetsUtil;
import com.example.penyyouquandemo.util.CommonUtils;
import com.example.penyyouquandemo.util.GlideSimpleTarget;
import com.example.penyyouquandemo.util.Utils;
import com.github.ielse.imagewatcher.ImageWatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import cn.bingoogolapple.baseadapter.BGAOnRVItemLongClickListener;
import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;
import cn.bingoogolapple.photopicker.activity.BGAPPToolbarActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.imageloader.BGARVOnScrollListener;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 你自己项目里「可以不继承 BGAPPToolbarActivity」，我在这里继承 BGAPPToolbarActivity 只是为了方便写 Demo
 * BGAOnRVItemClickListener和BGAOnRVItemLongClickListener这两个接口是为了测试事件传递是否正确，你自己的项目里可以不实现这两个接口
 */
public class MomentListActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, BGANinePhotoLayout.Delegate, BGAOnRVItemClickListener, BGAOnRVItemLongClickListener,
        ImageWatcher.Loader {

    private static final int PRC_PHOTO_PREVIEW = 1;

    private static final int RC_ADD_MOMENT = 1;

    private RecyclerView mMomentRv;
    private MomentAdapter mMomentAdapter;
    private CircleAdapter circleAdapter;

    private List<CircleBean.DataBean> dataBeans;
    private ImageWatcher imageWatcher;
    private LinearLayout llComment;
    private EditText etComment;

    /**
     * 设置图片预览时是否具有保存图片功能「测试接口用的」
     */
    private CheckBox mDownLoadableCb;

    private BGANinePhotoLayout mCurrentClickNpl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_list);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        mDownLoadableCb = findViewById(R.id.cb_moment_list_downloadable);
        llComment = findViewById(R.id.ll_comment);
        etComment = findViewById(R.id.et_comment);
        mMomentRv = findViewById(R.id.rv_moment_list_moments);
        imageWatcher = findViewById(R.id.imageWatcher);
        //初始化仿微信图片滑动加载器
        imageWatcher.setTranslucentStatus(Utils.calcStatusBarHeight(this));
        imageWatcher.setErrorImageRes(R.mipmap.error_picture);
        imageWatcher.setOnPictureLongPressListener((ImageWatcher.OnPictureLongPressListener) this);
        imageWatcher.setLoader((ImageWatcher.Loader) this);
    }

    private void initData() {
        dataBeans = new ArrayList<>();
        dataBeans = AssetsUtil.getStates(this);

        mMomentRv.setLayoutManager(new LinearLayoutManager(this));
//        mMomentRv.setAdapter(mMomentAdapter);

        circleAdapter = new CircleAdapter(dataBeans, imageWatcher, llComment, etComment, (CircleAdapter.Click) this);
        mMomentRv.setAdapter(circleAdapter);
    }


    private void setListener() {
        mMomentAdapter = new MomentAdapter(mMomentRv);
        mMomentAdapter.setOnRVItemClickListener(this);
        mMomentAdapter.setOnRVItemLongClickListener(this);

        mMomentRv.addOnScrollListener(new BGARVOnScrollListener(this));

        mMomentRv.setOnTouchListener((view, motionEvent) -> {
            if (llComment.getVisibility() == View.VISIBLE) {
                updateEditTextBodyVisible(View.GONE);
                return true;
            }
            return false;
        });
    }

//    @Override
//    protected void processLogic(Bundle savedInstanceState) {
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        setTitle("朋友圈列表");
//
//        mMomentRv.setLayoutManager(new LinearLayoutManager(this));
////        mMomentRv.setAdapter(mMomentAdapter);
//
//        circleAdapter = new CircleAdapter(dataBeans, imageWatcher, llComment, etComment, this);
//        mMomentRv.setAdapter(circleAdapter);
//
//        addNetImageTestData();
//    }

    public void updateEditTextBodyVisible(int visibility) {
        llComment.setVisibility(visibility);
        if (View.VISIBLE == visibility) {
            llComment.requestFocus();
            //弹出键盘
            CommonUtils.showSoftInput(etComment.getContext(), etComment);

        } else if (View.GONE == visibility) {
            //隐藏键盘
            CommonUtils.hideSoftInput(etComment.getContext(), etComment);
        }
    }

    /**
     * 添加网络图片测试数据
     */
    private void addNetImageTestData() {
        List<Moment> moments = new ArrayList<>();

        moments.add(new Moment("1张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered1.png"))));
        moments.add(new Moment("2张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered2.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered3.png"))));
        moments.add(new Moment("9张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered16.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered17.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered18.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered19.png"))));
        moments.add(new Moment("5张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png"))));
        moments.add(new Moment("3张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered4.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered5.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered6.png"))));
        moments.add(new Moment("8张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered16.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered17.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered18.png"))));
        moments.add(new Moment("4张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered7.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered8.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered9.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered10.png"))));
        moments.add(new Moment("2张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered2.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered3.png"))));
        moments.add(new Moment("3张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered4.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered5.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered6.png"))));
        moments.add(new Moment("4张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered7.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered8.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered9.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered10.png"))));
        moments.add(new Moment("9张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered16.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered17.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered18.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered19.png"))));
        moments.add(new Moment("1张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered1.png"))));
        moments.add(new Moment("5张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png"))));
        moments.add(new Moment("6张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered16.png"))));
        moments.add(new Moment("7张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered16.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered17.png"))));
        moments.add(new Moment("8张网络图片", new ArrayList<>(Arrays.asList("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered11.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered12.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered13.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered14.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered15.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered16.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered17.png", "http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered18.png"))));
        ArrayList<String> photos = new ArrayList<>();
        for (int i = 1; i < 19; i++) {
            photos.add("http://bgashare.bingoogolapple.cn/refreshlayout/images/staggered" + i + ".png");
        }
        moments.add(new Moment("18张网络图片", photos));

        mMomentAdapter.setData(moments);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.tv_moment_list_add) {
            startActivityForResult(new Intent(this, MomentAddActivity.class), RC_ADD_MOMENT);
        } else if (v.getId() == R.id.tv_moment_list_system) {
            startActivity(new Intent(this, SystemGalleryActivity.class));
        }
    }

    @Override
    public void load(Context context, Uri uri, ImageWatcher.LoadCallback loadCallback) {
        Glide.with(context).asBitmap().load(uri.toString()).into(new GlideSimpleTarget(loadCallback));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RC_ADD_MOMENT) {
            mMomentAdapter.addFirstItem(MomentAddActivity.getMoment(data));
            mMomentRv.smoothScrollToPosition(0);
        }
    }

    /**
     * 图片预览，兼容6.0动态权限
     */
    @AfterPermissionGranted(PRC_PHOTO_PREVIEW)
    private void photoPreviewWrapper() {
        if (mCurrentClickNpl == null) {
            return;
        }

        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            File downloadDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerDownload");
            BGAPhotoPreviewActivity.IntentBuilder photoPreviewIntentBuilder = new BGAPhotoPreviewActivity.IntentBuilder(this);

            if (mDownLoadableCb.isChecked()) {
                // 保存图片的目录，如果传 null，则没有保存图片功能
                photoPreviewIntentBuilder.saveImgDir(downloadDir);
            }

            if (mCurrentClickNpl.getItemCount() == 1) {
                // 预览单张图片
                photoPreviewIntentBuilder.previewPhoto(mCurrentClickNpl.getCurrentClickItem());
            } else if (mCurrentClickNpl.getItemCount() > 1) {
                // 预览多张图片
                photoPreviewIntentBuilder.previewPhotos(mCurrentClickNpl.getData())
                        .currentPosition(mCurrentClickNpl.getCurrentClickItemPosition()); // 当前预览图片的索引
            }
            startActivity(photoPreviewIntentBuilder.build());
        } else {
            EasyPermissions.requestPermissions(this, "图片预览需要以下权限:\n\n1.访问设备上的照片", PRC_PHOTO_PREVIEW, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == PRC_PHOTO_PREVIEW) {
            Toast.makeText(this, "您拒绝了「图片预览」所需要的相关权限!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        mCurrentClickNpl = ninePhotoLayout;
        photoPreviewWrapper();
    }

    @Override
    public void onClickExpand(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        ninePhotoLayout.setIsExpand(true);
        ninePhotoLayout.flushItems();
    }

    @Override
    public void onRVItemClick(ViewGroup viewGroup, View view, int position) {
        Toast.makeText(this, "点击了item " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onRVItemLongClick(ViewGroup viewGroup, View view, int position) {
        Toast.makeText(this, "长按了item " + position, Toast.LENGTH_SHORT).show();
        return true;
    }

    private class MomentAdapter extends BGARecyclerViewAdapter<Moment> {

        public MomentAdapter(RecyclerView recyclerView) {
            super(recyclerView, R.layout.item_moment);
        }

        @Override
        protected void fillData(BGAViewHolderHelper helper, int position, Moment moment) {
            if (TextUtils.isEmpty(moment.content)) {
                helper.setVisibility(R.id.tv_item_moment_content, View.GONE);
            } else {
                helper.setVisibility(R.id.tv_item_moment_content, View.VISIBLE);
                helper.setText(R.id.tv_item_moment_content, moment.content);
            }

            BGANinePhotoLayout ninePhotoLayout = helper.getView(R.id.npl_item_moment_photos);
            ninePhotoLayout.setDelegate(MomentListActivity.this);
            ninePhotoLayout.setData(moment.photos);
        }
    }
}