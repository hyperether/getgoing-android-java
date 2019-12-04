package com.hyperether.getgoing.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.crashlytics.android.Crashlytics;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.databinding.ActivityMainBinding;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.ui.adapter.HorizontalListAdapter;
import com.hyperether.getgoing.ui.fragment.ActivitiesFragment;
import com.hyperether.getgoing.ui.fragment.ProfileFragment;
import com.hyperether.getgoing.ui.fragment.old.SettingsFragment;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.Conversion;

import io.fabric.sdk.android.Fabric;

import static com.hyperether.getgoing.ui.fragment.old.SettingsFragment.DATA_KEY;

public class GetGoingActivity extends AppCompatActivity implements
        SettingsFragment.SettingsFragmentListener {

    private static final int WALK_ID = 1;
    private static final int RUN_ID = 2;
    private static final int RIDE_ID = 3;
    //private static final int IMPERIAL = 1;
    //private static final int US = 2;

    public static float ratio = (float) 0.0;

    private ActivityMainBinding mBinding;
    private CBDataFrame cbDataFrameLocal;
    private SnapHelper snapHelper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private CircleProgressBar circleProgressBar;
    private HorizontalListAdapter mAdapter;
    private ImageView blueRectangle;
    private ImageView selectorView;
    private ImageView selectorViewTransparent;
    private ImageView blueRecActivity;
    private TextView blueSentence;
    private TextView actLabel;
    private View curvedBottomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        cbDataFrameLocal = new CBDataFrame();
        mBinding.setViewModel(new ClickHandler());

        actLabel = findViewById(R.id.tv_ma_mainact);
        selectorView = findViewById(R.id.imageView2);
        selectorViewTransparent = findViewById(R.id.iv_am_circletransparent);
        circleProgressBar = findViewById(R.id.cpb_am_kmgoal);

        initScreenDimen();
        initRecyclerView();
        initListeners();
        initProgressBars();

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, Constants
                .TAG_CODE_PERMISSION_LOCATION);

        SharedPreferences currentSettings = getSharedPreferences(Constants.PREF_FILE, 0);

        /*
         * default value is metric
         */
//        int measureUnitId = currentSettings.getInt("measurementSystemId", Constants.METRIC);
//        cbDataFrameLocal.setMeasurementSystemId(measureUnitId);
//        int age = currentSettings.getInt("age", 0);
//        cbDataFrameLocal.setAge(age);
//        int weight = currentSettings.getInt("weight", 0);
//        cbDataFrameLocal.setWeight(weight);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.TAG_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (Constants.RESULT_REQUESTED):
                if (resultCode == Activity.RESULT_OK) {
                    if (data.hasExtra(DATA_KEY)) {
                        this.cbDataFrameLocal = data.getParcelableExtra(DATA_KEY);
                        SharedPreferences settings = getSharedPreferences(
                                Constants.PREF_FILE, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("measurementSystemId",
                                this.cbDataFrameLocal.getMeasurementSystemId());
                        editor.putInt("age", this.cbDataFrameLocal.getAge());
                        editor.putInt("weight", this.cbDataFrameLocal.getWeight());
                        editor.apply();

                        int id = settings.getInt("meteringActivityRequestedId", 0);
                        if (id > 0) {
                            callMeteringActivity(id);
                        }
                    }
                }
                break;
        }
    }

    /*
     * true: parameters are set false: settings required
     */
    private boolean getParametersStatus(CBDataFrame cbDataFrameLocal) {
        return !((cbDataFrameLocal.getAge() == 0)
                || (cbDataFrameLocal.getWeight() == 0));
    }

    /**
     * This method starts SettingsFragment
     */
    private void callSettingsFragment() {
//        SettingsFragment settingsFragment = SettingsFragment.newInstance(this.cbDataFrameLocal);
//        getFragmentManager()
//                .beginTransaction()
//                .replace(R.id.activity_main, settingsFragment)
//                .commit();
    }

    /**
     * This method starts SettingsFragment
     *
     * @param id mode id
     */
    private void callMeteringActivity(int id) {
        if (getParametersStatus(this.cbDataFrameLocal)) {
            setMeteringActivityRequested(0);
            this.cbDataFrameLocal.setProfileId(id);
            Intent intent = new Intent(GetGoingActivity.this, ShowLocationActivity.class);
            CacheManager.getInstance().setObDataFrameLocal(this.cbDataFrameLocal);
            intent.putExtra("searchKey", this.cbDataFrameLocal);
            startActivity(intent);
        } else {
            setMeteringActivityRequested(id);
            callSettingsFragment();
        }
    }

    /**
     * This method set Metering Activity request.
     *
     * @param id mode id
     */
    private void setMeteringActivityRequested(int id) {
        SharedPreferences settings = getSharedPreferences(Constants.PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("meteringActivityRequestedId", id);
        editor.apply();
    }

    private void initRecyclerView()
    {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        SparseIntArray DRAWABLE_MAP = new SparseIntArray();
        DRAWABLE_MAP.append(R.drawable.ic_light_bicycling_icon_inactive, R.drawable.ic_light_bicycling_icon_active);
        DRAWABLE_MAP.append(R.drawable.ic_light_running_icon_inactive, R.drawable.ic_light_running_icon_active);
        DRAWABLE_MAP.append(R.drawable.ic_light_walking_icon, R.drawable.ic_light_walking_icon_active);

        mAdapter = new HorizontalListAdapter(DRAWABLE_MAP, getApplicationContext());
        recyclerView.setAdapter(mAdapter);

        //((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(15, 0);

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        float screenwidth = Conversion.convertPixelToDp(this, getApplicationContext().getResources().getDisplayMetrics().widthPixels);
        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(layoutManager.getItemCount() / 2, /*(- Conversion.convertDpToPixel(50, this)) / 2*/-1);
    }

    @Deprecated
    private void initListeners()
    {
        ImageView ib_am_user = findViewById(R.id.ib_am_user);
        ImageView iv_am_arrows = findViewById(R.id.iv_am_arrow2actfrag);

        ib_am_user.setOnClickListener(view -> {
            ProfileFragment profileFragment = ProfileFragment.newInstance(null);
            profileFragment.show(getSupportFragmentManager(), "ProfileFragment");
        });

        iv_am_arrows.setOnClickListener(view -> {
            ActivitiesFragment activitiesFragment = ActivitiesFragment.newInstance(null);
            activitiesFragment.show(getSupportFragmentManager(), "ActivitiesFragment");
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            int i = 0;
            int[] centralImgPos = new int[2];
            int[] selectorViewPos = new int[2];

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Integer k1 = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                ImageView centralImg = layoutManager.findViewByPosition(k1).findViewById(R.id.iv_ri_pic);

                if (centralImg.getTag().equals(R.drawable.ic_light_bicycling_icon_inactive))
                    actLabel.setText("Cycling");
                else if (centralImg.getTag().equals(R.drawable.ic_light_running_icon_inactive))
                    actLabel.setText("Running");
                else if (centralImg.getTag().equals(R.drawable.ic_light_walking_icon))
                    actLabel.setText("Walking");

                centralImg.getLocationOnScreen(centralImgPos);

                if (i++ == 0)
                {
                    selectorView.getLocationOnScreen(selectorViewPos);
                }

                int centralImgWidthParam = centralImg.getLayoutParams().width / 2;

                if (centralImgPos[0] > selectorViewPos[0] - centralImgWidthParam && centralImgPos[0] < selectorViewPos[0] + centralImgWidthParam)
                {
                    if (centralImg.getTag().equals(R.drawable.ic_light_bicycling_icon_inactive))
                    {
                        centralImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_bicycling_icon_active));
                        centralImg.setTag(R.drawable.ic_light_bicycling_icon_active);
                    }
                    else if (centralImg.getTag().equals(R.drawable.ic_light_running_icon_inactive))
                    {
                        centralImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_icon_active));
                        centralImg.setTag(R.drawable.ic_light_running_icon_active);
                    }
                    else if (centralImg.getTag().equals(R.drawable.ic_light_walking_icon))
                    {
                        centralImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_walking_icon_active));
                        centralImg.setTag(R.drawable.ic_light_walking_icon_active);
                    }
                }

                ImageView leftImg, rightImg;

                try {
                    leftImg = layoutManager.findViewByPosition(k1 - 1).findViewById(R.id.iv_ri_pic);

                    if (leftImg.getTag().equals(R.drawable.ic_light_bicycling_icon_active))
                    {
                        leftImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_bicycling_icon_inactive));
                        leftImg.setTag(R.drawable.ic_light_bicycling_icon_inactive);
                    }
                    else if (leftImg.getTag().equals(R.drawable.ic_light_running_icon_active))
                    {
                        leftImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_icon_inactive));
                        leftImg.setTag(R.drawable.ic_light_running_icon_inactive);
                    }
                    else if (leftImg.getTag().equals(R.drawable.ic_light_walking_icon_active))
                    {
                        leftImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_walking_icon));
                        leftImg.setTag(R.drawable.ic_light_walking_icon);
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                try {
                    rightImg = layoutManager.findViewByPosition(k1 + 1).findViewById(R.id.iv_ri_pic);

                    if (rightImg.getTag().equals(R.drawable.ic_light_bicycling_icon_active))
                    {
                        rightImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_bicycling_icon_inactive));
                        rightImg.setTag(R.drawable.ic_light_bicycling_icon_inactive);
                    }
                    else if (rightImg.getTag().equals(R.drawable.ic_light_running_icon_active))
                    {
                        rightImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_icon_inactive));
                        rightImg.setTag(R.drawable.ic_light_running_icon_inactive);
                    }
                    else if (rightImg.getTag().equals(R.drawable.ic_light_walking_icon_active))
                    {
                        rightImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_light_walking_icon));
                        rightImg.setTag(R.drawable.ic_light_walking_icon);
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void initProgressBars()
    {
        circleProgressBar.setProgress(42);
    }

    private void initScreenDimen()
    {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        ratio = (float)metrics.heightPixels / (float)metrics.widthPixels;

        curvedBottomView = findViewById(R.id.customBottomBar);
        blueRectangle = findViewById(R.id.iv_am_bluerectangle);
        blueSentence = findViewById(R.id.tv_am_burn);
        blueRecActivity = findViewById(R.id.iv_am_activity);

        if (ratio >= 1.8)
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blueSentence.getLayoutParams();

            blueRectangle.getLayoutParams().height = (int) (circleProgressBar.getLayoutParams().height + circleProgressBar.getLayoutParams().height * 0.3);
            blueRectangle.getLayoutParams().height = 650;
            params.bottomMargin = 100;
            blueSentence.setLayoutParams(params);
        }
    }

    @Override
    public void onDataSent(CBDataFrame dataFrame) {
        cbDataFrameLocal = dataFrame;
    }

    public class ClickHandler {

        public void onWalk(View view) {
            callMeteringActivity(WALK_ID);
        }

        public void onRun(View view) {
            callMeteringActivity(RUN_ID);
        }

        public void onRide(View view) {
            callMeteringActivity(RIDE_ID);
        }

        public void onProfileClick() {
            ProfileFragment profileFragment = ProfileFragment.newInstance(null);
            profileFragment.show(getSupportFragmentManager(), "ProfileFragment");
        }
    }

    private static final class MyProgressFormatter implements CircleProgressBar.ProgressFormatter {
        private static final String DEFAULT_PATTERN = "%d%%";

        @SuppressLint("DefaultLocale")
        @Override
        public CharSequence format(int progress, int max) {
            return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
        }
    }

}
