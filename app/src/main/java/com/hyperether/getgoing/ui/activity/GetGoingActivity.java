package com.hyperether.getgoing.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.crashlytics.android.Crashlytics;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.databinding.ActivityMainBinding;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.adapter.HorizontalListAdapter;
import com.hyperether.getgoing.ui.adapter.formatter.MyProgressFormatter;
import com.hyperether.getgoing.ui.adapter.formatter.MyProgressFormatter2;
import com.hyperether.getgoing.ui.adapter.formatter.MyProgressFormatter3;
import com.hyperether.getgoing.ui.fragment.ActivitiesFragment;
import com.hyperether.getgoing.ui.fragment.ProfileFragment;
import com.hyperether.getgoing.ui.fragment.old.SettingsFragment;
import com.hyperether.getgoing.util.Constants;

import java.util.ArrayList;
import java.util.List;

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

    /*USER DATA VARIABLES*/
    private int measureUnitId;

    private ActivityMainBinding mBinding;
    private CBDataFrame cbDataFrameLocal;
    private SnapHelper snapHelper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private CircleProgressBar circleProgressBar, circleProgressBar2, circleProgressBar3;
    private HorizontalListAdapter mAdapter;
    private ImageView blueRectangle;
    private ImageView selectorView;
    private ImageView centralImg;
    private TextView blueSentence;
    private TextView actLabel, lastExeLabel;

    private SharedPreferences currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setViewModel(new ClickHandler());

        cbDataFrameLocal = CacheManager.getInstance().getObDataFrameGlobal();

        actLabel = findViewById(R.id.tv_ma_mainact);
        selectorView = findViewById(R.id.imageView2);
        circleProgressBar = findViewById(R.id.cpb_am_kmgoal);
        circleProgressBar2 = findViewById(R.id.cpb_am_kmgoal1);
        circleProgressBar3 = findViewById(R.id.cpb_am_kmgoal2);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, Constants
                .TAG_CODE_PERMISSION_LOCATION);

        currentSettings = getSharedPreferences(Constants.PREF_FILE, 0);

        zeroNodeInit();
        initScreenDimen();
        initRecyclerView();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initModel();
        initProgressBars();
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
                    }
                }
                break;
        }
    }

    private void zeroNodeInit()
    {
        if (currentSettings.getBoolean("zeroNode", false) == false)
        {
            /*route init*/
            List<DbNode> tmpRoute = new ArrayList<>();
            DbNode tmpNode = new DbNode(0, 0, 0, 0, 0, 0);
            tmpRoute.add(tmpNode);
            roomStoreNodeZero(tmpRoute);

            SharedPreferences.Editor edit = currentSettings.edit();
            edit.putBoolean("zeroNode", true);
            edit.apply();
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
     * This method starts ProfileFragment when user hasn't entered his data
     */
    private void callProfileFragment() {
        ProfileFragment profileFragment = ProfileFragment.newInstance(null);
        profileFragment.show(getSupportFragmentManager(), "ProfileFragment");
    }

    /**
     * This method starts tracking
     *
     * @param id mode id
     */
    private void callMeteringActivity(int id) {
        if (getParametersStatus(CacheManager.getInstance().getObDataFrameGlobal())) {
            setMeteringActivityRequested(0);
            this.cbDataFrameLocal.setProfileId(id);
            Intent intent = new Intent(GetGoingActivity.this, ShowLocationActivity.class);
            intent.putExtra("searchKey", CacheManager.getInstance().getObDataFrameGlobal());
            startActivity(intent);
        } else {
            setMeteringActivityRequested(id);
            Toast.makeText(this, "You must enter your data first!", Toast.LENGTH_LONG).show();
            callProfileFragment();
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

    private void initModel() {
        /*
         * default value is metric
         */
        measureUnitId = currentSettings.getInt("measurementSystemId", Constants.METRIC);
        cbDataFrameLocal.setMeasurementSystemId(measureUnitId);
        cbDataFrameLocal.setHeight(currentSettings.getInt("height", 0));
        cbDataFrameLocal.setWeight(currentSettings.getInt("weight", 0));
        cbDataFrameLocal.setAge(currentSettings.getInt("age", 0));
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

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(layoutManager.getItemCount() / 2,-1);
    }

    @Deprecated
    private void initListeners()
    {
        ImageView ib_am_user = findViewById(R.id.ib_am_user);
        ImageView iv_am_arrows = findViewById(R.id.iv_am_arrow2actfrag);
        TextView tv_am_viewall = findViewById(R.id.tv_am_viewall);
        Button startBtn = findViewById(R.id.materialButton);


        ib_am_user.setOnClickListener(view -> {
            ProfileFragment profileFragment = ProfileFragment.newInstance(null);
            profileFragment.show(getSupportFragmentManager(), "ProfileFragment");
        });

        iv_am_arrows.setOnClickListener(view -> {
            ActivitiesFragment activitiesFragment = ActivitiesFragment.newInstance(null);
            activitiesFragment.show(getSupportFragmentManager(), "ActivitiesFragment");
        });

        tv_am_viewall.setOnClickListener(view -> {
            ActivitiesFragment activitiesFragment = ActivitiesFragment.newInstance(null);
            activitiesFragment.show(getSupportFragmentManager(), "ActivitiesFragment");
        });

        startBtn.setOnClickListener(view -> {
            if (centralImg.getTag().equals(R.drawable.ic_light_walking_icon_active))
                callMeteringActivity(WALK_ID);
            else if (centralImg.getTag().equals(R.drawable.ic_light_running_icon_active))
                callMeteringActivity(RUN_ID);
            else if (centralImg.getTag().equals(R.drawable.ic_light_bicycling_icon_active))
                callMeteringActivity(RIDE_ID);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            int i = 0;
            int[] centralImgPos = new int[2];
            int[] selectorViewPos = new int[2];

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                View centralLayout = findCenterView(layoutManager, OrientationHelper.createOrientationHelper(layoutManager, RecyclerView.HORIZONTAL));
                centralImg = centralLayout.findViewById(R.id.iv_ri_pic);
                int k1 = layoutManager.getPosition(centralLayout);

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

    public void initProgressBars()
    {
        new PullProgressData().execute(null, null, null);
    }

    private void initScreenDimen()
    {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        ratio = (float)metrics.heightPixels / (float)metrics.widthPixels;

        blueRectangle = findViewById(R.id.iv_am_bluerectangle);
        blueSentence = findViewById(R.id.tv_am_burn);
        lastExeLabel = findViewById(R.id.tv_am_lastexercise);

        if (ratio >= 1.8)
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blueSentence.getLayoutParams();
            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) blueRectangle.getLayoutParams();
            ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) lastExeLabel.getLayoutParams();

            blueRectangle.getLayoutParams().height = (int) (circleProgressBar.getLayoutParams().height + circleProgressBar.getLayoutParams().height * 0.3);
            blueRectangle.getLayoutParams().height = 650;

            params.bottomMargin = 150;
            params1.topMargin = 30;
            params2.topMargin = 80;

            blueSentence.setLayoutParams(params);
            blueRectangle.setLayoutParams(params1);
            lastExeLabel.setLayoutParams(params2);
        }
    }

    private View findCenterView(RecyclerView.LayoutManager layoutManager,
                                OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            /** if child center is closer than previous closest, set it as closest  **/
            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }
        return closestChild;
    }

    @Override
    public void onDataSent(CBDataFrame dataFrame) {
        cbDataFrameLocal = dataFrame;
    }

    private void roomStoreNodeZero(List<DbNode> nodeList) {
        DbRoute dbRoute = new DbRoute(0, 0,0,0,"null", 0, 1, 0);
        DbHelper.getInstance(getApplicationContext()).insertRoute(dbRoute, nodeList);
    }

    private class PullProgressData extends AsyncTask<Void, Void, Void>
    {
        List<DbRoute> pointerList;
        TextView kcalVal, progBar1Act;
        ImageView progBar1Img;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            kcalVal = findViewById(R.id.tv_am_kcalval);
            progBar1Act = findViewById(R.id.tv_am_progbar_act);
            progBar1Img = findViewById(R.id.iv_am_activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            pointerList = new ArrayList<>();
            DbHelper.getInstance(getApplicationContext()).getLastRoute(pointerList);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!pointerList.isEmpty())
            {
                int lastRouteLen = (int) pointerList.get(0).getLength();
                int lastRouteTime;

                if (pointerList.get(0).getDuration() >= 60000)
                    lastRouteTime = Math.round(pointerList.get(0).getDuration() / 1000 / 60);
                else
                    lastRouteTime = 0;

                int goal = (int) pointerList.get(0).getGoal();
                int cpbProgress;

                if (goal != 0)
                    cpbProgress = Math.round(lastRouteLen * 100 / goal);
                else
                    cpbProgress = 0;

                int kcal = (int) pointerList.get(0).getEnergy();

                circleProgressBar.setProgressFormatter(new MyProgressFormatter((double) lastRouteLen));
                circleProgressBar.setProgress(cpbProgress);

                circleProgressBar2.setProgressFormatter(new MyProgressFormatter2(lastRouteTime));
                circleProgressBar2.setProgress(100);

                circleProgressBar3.setProgressFormatter(new MyProgressFormatter3());

                kcalVal.setText(String.valueOf(kcal));

                switch (pointerList.get(0).getActivity_id())
                {
                    case 1:
                    {
                        progBar1Act.setText("Walking");
                        progBar1Img.setImageDrawable(getResources().getDrawable(R.drawable.ic_walking_icon));
                        break;
                    }
                    case 2:
                    {
                        progBar1Act.setText("Running");
                        progBar1Img.setImageDrawable(getResources().getDrawable(R.drawable.ic_running_icon));
                        break;
                    }
                    case 3:
                    {
                        progBar1Act.setText("Cycling");
                        progBar1Img.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_bicycling_icon));
                        break;
                    }
                }
            }
            else
                resetDisplay();
        }

        private void resetDisplay()
        {
            circleProgressBar.setProgressFormatter(new MyProgressFormatter(0));
            circleProgressBar.setProgress(0);

            circleProgressBar2.setProgressFormatter(new MyProgressFormatter2(0));
            circleProgressBar2.setProgress(0);

            circleProgressBar3.setProgressFormatter(new MyProgressFormatter3());

            kcalVal.setText(Integer.toString(0));
        }
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

}
