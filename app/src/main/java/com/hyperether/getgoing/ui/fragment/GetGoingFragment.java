package com.hyperether.getgoing.ui.fragment;


import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.databinding.FragmentGetgoingBindingImpl;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.Node;
import com.hyperether.getgoing.repository.room.entity.Route;
import com.hyperether.getgoing.ui.adapter.ModeListAdapter;
import com.hyperether.getgoing.ui.formatter.TimeProgressFormatterInvisible;
import com.hyperether.getgoing.util.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

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

import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_STARTED;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_GG_ACT;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_KEY;
import static com.hyperether.getgoing.util.Constants.TRACKING_ACTIVITY_KEY;


public class GetGoingFragment extends Fragment {
    private NavController navigationController;
    public static float ratio = (float) 0.0;
    private FragmentGetgoingBindingImpl mBinding;
    private SnapHelper snapHelper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ModeListAdapter mAdapter;
    private ImageView blueRectangle;
    private ImageView selectorView;
    private ImageView centralImg;
    private TextView blueSentence;
    private TextView actLabel, lastExeLabel;

    private long lastRoute;

    public GetGoingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_getgoing, container, false);
        SharedPref.setMeasurementSystemId(0);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigationController = Navigation.findNavController(view);
        if (ServiceUtil.isServiceActive(requireContext())) {
            callTrackingFragment(ACTIVITY_STARTED);
        }

        mBinding.cpbAmKmgoal2.setProgressFormatter(new TimeProgressFormatterInvisible());
        actLabel = getView().findViewById(R.id.tv_ma_mainact);
        blueSentence = getView().findViewById(R.id.tv_am_burn);
        selectorView = getView().findViewById(R.id.imageView2);

        zeroNodeInit();
        initScreenDimen();
        initRecyclerView();
        initListeners();
    }

    private void zeroNodeInit() {
        if (!SharedPref.isZeroNodeInit()) {
            /*route init*/
            List<Node> tmpRoute = new ArrayList<>();
            Node tmpNode = new Node(0, 0, 0, 0, 0, 0);
            tmpRoute.add(tmpNode);
            roomStoreNodeZero(tmpRoute);

            SharedPref.setZeroNodeInit(true);
            // no saved routes yet
            SharedPref.setWalkRouteExisting(false);
            SharedPref.setRunRouteExisting(false);
            SharedPref.setRideRouteExisting(false);
        } else {
            GgRepository.getInstance().getLastRouteAsLiveData().observe(getActivity(), new Observer<Route>() {
                @Override
                public void onChanged(Route route) {

                    mBinding.setLastRoute(route);
                    if (route == null) {
                        lastRoute = 0;
                    } else {
                        lastRoute = route.getActivity_id();
                    }
                }
            });
        }
    }

    /*
     * true: parameters are set false: settings required
     */
    private boolean getParametersStatus() {
        return !((SharedPref.getAge() == 0) || (SharedPref.getWeight() == 0));
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
        if (getParametersStatus()) {
            Bundle bundle = new Bundle();
            bundle.putInt(TRACKING_ACTIVITY_KEY, id);
            navigationController.navigate(R.id.action_getGoingFragment_to_trackingFragment, bundle);
        } else {
            callProfileFragment();
        }
    }

    private void callActivitiesFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt(OPENED_FROM_KEY, OPENED_FROM_GG_ACT);
        navigationController.navigate(R.id.action_getGoingFragment_to_activitiesFragment, bundle);
    }

    private void initRecyclerView() {
        recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        ((LinearLayoutManager) layoutManager).setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        SparseIntArray modesList = new SparseIntArray();
        modesList.append(R.drawable.ic_bicycling_gray, R.drawable.ic_bicycling_blue);
        modesList.append(R.drawable.ic_light_running_gray, R.drawable.ic_light_running_blue);
        modesList.append(R.drawable.ic_walking_gray, R.drawable.ic_walking_blue);

        mAdapter = new ModeListAdapter(modesList, getActivity().getApplicationContext());
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
            if (centralImg.getTag().equals(R.drawable.ic_walking_blue))
                callTrackingFragment(ACTIVITY_WALK_ID);
            else if (centralImg.getTag().equals(R.drawable.ic_light_running_blue))
                callTrackingFragment(ACTIVITY_RUN_ID);
            else if (centralImg.getTag().equals(R.drawable.ic_bicycling_blue))
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

                if (centralImg.getTag().equals(R.drawable.ic_bicycling_gray))
                    actLabel.setText(getString(R.string.cycling));
                else if (centralImg.getTag().equals(R.drawable.ic_light_running_gray))
                    actLabel.setText(getString(R.string.running));
                else if (centralImg.getTag().equals(R.drawable.ic_walking_gray))
                    actLabel.setText(getString(R.string.walking));

                centralImg.getLocationOnScreen(centralImgPos);

                if (i++ == 0) {
                    selectorView.getLocationOnScreen(selectorViewPos);
                }

                int centralImgWidthParam = centralImg.getLayoutParams().width / 2;

                if (centralImgPos[0] > selectorViewPos[0] - centralImgWidthParam && centralImgPos[0] < selectorViewPos[0] + centralImgWidthParam) {
                    if (centralImg.getTag().equals(R.drawable.ic_bicycling_gray)) {
                        centralImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_bicycling_blue));
                        centralImg.setTag(R.drawable.ic_bicycling_blue);
                    } else if (centralImg.getTag().equals(R.drawable.ic_light_running_gray)) {
                        centralImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_blue));
                        centralImg.setTag(R.drawable.ic_light_running_blue);
                    } else if (centralImg.getTag().equals(R.drawable.ic_walking_gray)) {
                        centralImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_walking_blue));
                        centralImg.setTag(R.drawable.ic_walking_blue);
                    }
                }

                ImageView leftImg, rightImg;

                try {
                    leftImg = layoutManager.findViewByPosition(k1 - 1).findViewById(R.id.iv_ri_pic);

                    if (leftImg.getTag().equals(R.drawable.ic_bicycling_blue)) {
                        leftImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_bicycling_gray));
                        leftImg.setTag(R.drawable.ic_bicycling_gray);
                    } else if (leftImg.getTag().equals(R.drawable.ic_light_running_blue)) {
                        leftImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_gray));
                        leftImg.setTag(R.drawable.ic_light_running_gray);
                    } else if (leftImg.getTag().equals(R.drawable.ic_walking_blue)) {
                        leftImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_walking_gray));
                        leftImg.setTag(R.drawable.ic_walking_gray);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                try {
                    rightImg = layoutManager.findViewByPosition(k1 + 1).findViewById(R.id.iv_ri_pic);

                    if (rightImg.getTag().equals(R.drawable.ic_bicycling_blue)) {
                        rightImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_bicycling_gray));
                        rightImg.setTag(R.drawable.ic_bicycling_gray);
                    } else if (rightImg.getTag().equals(R.drawable.ic_light_running_blue)) {
                        rightImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_light_running_gray));
                        rightImg.setTag(R.drawable.ic_light_running_gray);
                    } else if (rightImg.getTag().equals(R.drawable.ic_walking_blue)) {
                        rightImg.setImageDrawable(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.ic_walking_gray));
                        rightImg.setTag(R.drawable.ic_walking_gray);
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

    private void roomStoreNodeZero(List<Node> nodeList) {
        Route route = new Route(0, 0, 0, 0, "null", 0, 0, 1, 0);
        GgRepository.getInstance().insertRouteInit(route, nodeList);
    }
}
