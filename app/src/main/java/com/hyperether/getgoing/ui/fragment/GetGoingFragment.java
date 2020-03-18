package com.hyperether.getgoing.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.databinding.FragmentGetgoingBindingImpl;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.adapter.HorizontalListAdapter;
import com.hyperether.getgoing.ui.formatter.TimeProgressFormatterInvisible;
import com.hyperether.getgoing.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_GG_ACT;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_KEY;
import static com.hyperether.getgoing.util.Constants.PREF_RIDE_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_RUN_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_WALK_ROUTE_EXISTING;


public class GetGoingFragment extends Fragment {

    private NavController navigationController;

    public static float ratio = (float) 0.0;

    /*USER DATA VARIABLES*/
    private int measureUnitId;

    private FragmentGetgoingBindingImpl mBinding;
    private CBDataFrame cbDataFrameLocal;
    private SnapHelper snapHelper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private HorizontalListAdapter mAdapter;
    private ImageView blueRectangle;
    private ImageView selectorView;
    private ImageView centralImg;
    private TextView blueSentence;
    private TextView actLabel, lastExeLabel;

    private SharedPreferences currentSettings;

    public GetGoingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_getgoing, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigationController = Navigation.findNavController(view);

        mBinding.cpbAmKmgoal2.setProgressFormatter(new TimeProgressFormatterInvisible());

        cbDataFrameLocal = CacheManager.getInstance().getObDataFrameGlobal();

        actLabel = getView().findViewById(R.id.tv_ma_mainact);
        blueSentence = getView().findViewById(R.id.tv_am_burn);
        selectorView = getView().findViewById(R.id.imageView2);

        currentSettings = getContext().getSharedPreferences(Constants.PREF_FILE, 0);

        zeroNodeInit();
        initScreenDimen();
        initRecyclerView();
        initListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        initModel();
    }

    private void zeroNodeInit() {
        if (!currentSettings.getBoolean("zeroNode", false)) {
            /*route init*/
            List<DbNode> tmpRoute = new ArrayList<>();
            DbNode tmpNode = new DbNode(0, 0, 0, 0, 0, 0);
            tmpRoute.add(tmpNode);
            roomStoreNodeZero(tmpRoute);

            SharedPreferences.Editor edit = currentSettings.edit();
            edit.putBoolean("zeroNode", true);

            // no saved routes yet
            edit.putBoolean(PREF_WALK_ROUTE_EXISTING, false);
            edit.putBoolean(PREF_RUN_ROUTE_EXISTING, false);
            edit.putBoolean(PREF_RIDE_ROUTE_EXISTING, false);

            edit.apply();

        } else {

            GgRepository.getInstance().getLastRoute().observe(getActivity(), new Observer<DbRoute>() {
                @Override
                public void onChanged(DbRoute dbRoute) {
                    mBinding.setLastRoute(dbRoute);
                }
            });
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
        navigationController.navigate(R.id.action_getGoingFragment_to_profileFragment);
    }

    /**
     * This method starts tracking
     *
     * @param id mode id
     */
    private void callTrackingFragment(int id) {
        if (getParametersStatus(CacheManager.getInstance().getObDataFrameGlobal())) {
            setMeteringActivityRequested(0);
            this.cbDataFrameLocal.setProfileId(id);
            navigationController.navigate(R.id.action_getGoingFragment_to_trackingFragment);
        } else {
            setMeteringActivityRequested(id);
            Toast.makeText(getActivity(), "You must enter your data first!", Toast.LENGTH_LONG).show();
            callProfileFragment();
        }
    }

    private void callActivitiesFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt(OPENED_FROM_KEY, OPENED_FROM_GG_ACT);
        navigationController.navigate(R.id.action_getGoingFragment_to_activitiesFragment, bundle);
    }

    /**
     * This method set Metering Activity request.
     *
     * @param id mode id
     */
    private void setMeteringActivityRequested(int id) {
        SharedPreferences settings = getContext().getSharedPreferences(Constants.PREF_FILE, 0);
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

    private void initRecyclerView() {
        recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        ((LinearLayoutManager) layoutManager).setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        SparseIntArray DRAWABLE_MAP = new SparseIntArray();
        DRAWABLE_MAP.append(R.drawable.ic_light_bicycling_icon_inactive, R.drawable.ic_light_bicycling_icon_active);
        DRAWABLE_MAP.append(R.drawable.ic_light_running_icon_inactive, R.drawable.ic_light_running_icon_active);
        DRAWABLE_MAP.append(R.drawable.ic_light_walking_icon, R.drawable.ic_light_walking_icon_active);

        mAdapter = new HorizontalListAdapter(DRAWABLE_MAP, getActivity().getApplicationContext());
        recyclerView.setAdapter(mAdapter);

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(layoutManager.getItemCount() / 2, -1);
    }

    @Deprecated
    private void initListeners() {
        ImageView ib_am_user = getView().findViewById(R.id.ib_am_user);
        TextView tv_am_viewall = getView().findViewById(R.id.tv_am_viewall);
        ImageView iv_am_bluerectangle = getView().findViewById(R.id.iv_am_bluerectangle);
        Button startBtn = getView().findViewById(R.id.materialButton);


        ib_am_user.setOnClickListener(view -> {
            callProfileFragment();
        });

        tv_am_viewall.setOnClickListener(view -> {
            callActivitiesFragment();
        });

        iv_am_bluerectangle.setOnClickListener(view -> {
            callActivitiesFragment();
        });

        startBtn.setOnClickListener(view -> {
            if (centralImg.getTag().equals(R.drawable.ic_light_walking_icon_active))
                callTrackingFragment(ACTIVITY_WALK_ID);
            else if (centralImg.getTag().equals(R.drawable.ic_light_running_icon_active))
                callTrackingFragment(ACTIVITY_RUN_ID);
            else if (centralImg.getTag().equals(R.drawable.ic_light_bicycling_icon_active))
                callTrackingFragment(ACTIVITY_RIDE_ID);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    actLabel.setText(getString(R.string.cycling));
                else if (centralImg.getTag().equals(R.drawable.ic_light_running_icon_inactive))
                    actLabel.setText(getString(R.string.running));
                else if (centralImg.getTag().equals(R.drawable.ic_light_walking_icon))
                    actLabel.setText(getString(R.string.walking));

                centralImg.getLocationOnScreen(centralImgPos);

                if (i++ == 0) {
                    selectorView.getLocationOnScreen(selectorViewPos);
                }

                int centralImgWidthParam = centralImg.getLayoutParams().width / 2;

                if (centralImgPos[0] > selectorViewPos[0] - centralImgWidthParam && centralImgPos[0] < selectorViewPos[0] + centralImgWidthParam) {
                    if (centralImg.getTag().equals(R.drawable.ic_light_bicycling_icon_inactive)) {
                        centralImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_bicycling_icon_active));
                        centralImg.setTag(R.drawable.ic_light_bicycling_icon_active);
                    } else if (centralImg.getTag().equals(R.drawable.ic_light_running_icon_inactive)) {
                        centralImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_icon_active));
                        centralImg.setTag(R.drawable.ic_light_running_icon_active);
                    } else if (centralImg.getTag().equals(R.drawable.ic_light_walking_icon)) {
                        centralImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_walking_icon_active));
                        centralImg.setTag(R.drawable.ic_light_walking_icon_active);
                    }
                }

                ImageView leftImg, rightImg;

                try {
                    leftImg = layoutManager.findViewByPosition(k1 - 1).findViewById(R.id.iv_ri_pic);

                    if (leftImg.getTag().equals(R.drawable.ic_light_bicycling_icon_active)) {
                        leftImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_bicycling_icon_inactive));
                        leftImg.setTag(R.drawable.ic_light_bicycling_icon_inactive);
                    } else if (leftImg.getTag().equals(R.drawable.ic_light_running_icon_active)) {
                        leftImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_icon_inactive));
                        leftImg.setTag(R.drawable.ic_light_running_icon_inactive);
                    } else if (leftImg.getTag().equals(R.drawable.ic_light_walking_icon_active)) {
                        leftImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_walking_icon));
                        leftImg.setTag(R.drawable.ic_light_walking_icon);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                try {
                    rightImg = layoutManager.findViewByPosition(k1 + 1).findViewById(R.id.iv_ri_pic);

                    if (rightImg.getTag().equals(R.drawable.ic_light_bicycling_icon_active)) {
                        rightImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_bicycling_icon_inactive));
                        rightImg.setTag(R.drawable.ic_light_bicycling_icon_inactive);
                    } else if (rightImg.getTag().equals(R.drawable.ic_light_running_icon_active)) {
                        rightImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_icon_inactive));
                        rightImg.setTag(R.drawable.ic_light_running_icon_inactive);
                    } else if (rightImg.getTag().equals(R.drawable.ic_light_walking_icon_active)) {
                        rightImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_walking_icon));
                        rightImg.setTag(R.drawable.ic_light_walking_icon);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initScreenDimen() {
        DisplayMetrics metrics = getActivity().getApplicationContext().getResources().getDisplayMetrics();
        ratio = (float) metrics.heightPixels / (float) metrics.widthPixels;

        blueRectangle = getView().findViewById(R.id.iv_am_bluerectangle);
        lastExeLabel = getView().findViewById(R.id.tv_am_lastexercise);

        int unicode = 0x1F605;  /* emoji */
        blueSentence.append(" " + String.valueOf(Character.toChars(unicode)));

        if (ratio >= 1.8) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blueSentence.getLayoutParams();
            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) blueRectangle.getLayoutParams();
            ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) lastExeLabel.getLayoutParams();

            blueRectangle.getLayoutParams().height = (int) (mBinding.cpbAmKmgoal.getLayoutParams().height + mBinding.cpbAmKmgoal.getLayoutParams().height * 0.3);
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

    private void roomStoreNodeZero(List<DbNode> nodeList) {
        DbRoute dbRoute = new DbRoute(0, 0, 0, 0, "null", 0, 1, 0);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList);
    }

}